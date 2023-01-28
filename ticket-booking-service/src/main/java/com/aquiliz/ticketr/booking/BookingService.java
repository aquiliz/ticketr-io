package com.aquiliz.ticketr.booking;

import com.aquiliz.ticketr.booking.dto.PricingRequest;
import com.aquiliz.ticketr.booking.dto.TicketBooking;
import com.aquiliz.ticketr.booking.dto.TicketBooking.Status;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Service
public class BookingService {

    private final TicketBookingRepository ticketBookingRepository;
    private final PricingServiceClient pricingServiceClient;
    private final BookingMessageSender bookingMessageSender;

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(
        new JavaTimeModule());

    public BookingService(TicketBookingRepository ticketBookingRepository,
        PricingServiceClient pricingServiceClient,
        BookingMessageSender bookingMessageSender) {
        this.ticketBookingRepository = ticketBookingRepository;
        this.pricingServiceClient = pricingServiceClient;
        this.bookingMessageSender = bookingMessageSender;
    }

    /**
     * Creates a new ticket booking and sends a creation message on the associated topic.
     *
     * @return the system-generated id of the newly created booking.
     */
    public String createBooking(@NonNull TicketBooking ticketBooking) {
        BigDecimal currentPrice = retrievePrice(ticketBooking);
        ticketBooking.setTotalPrice(currentPrice);
        ticketBooking.setStatus(Status.ACTIVE);
        ticketBooking.setTimeOfBooking(Instant.now());

        TicketBooking saved = ticketBookingRepository.save(ticketBooking);
        log.info("Created new booking: id={} , orig={} , dest={}, number of passengers={}",
            saved.getId(), saved.getOriginAirport(),
            saved.getDestinationAirport(), saved.getPassengers().size());
        bookingMessageSender.notifyForSavedBooking(saved);
        return saved.getId();
    }

    @Transactional
    @CachePut(value = "bookings", key = "#ticketBooking.id")
    public TicketBooking updateBooking(@NonNull TicketBooking ticketBooking) {
        if (StringUtils.isBlank(ticketBooking.getId())) {
            throw new IllegalArgumentException("ID is required in order to update ticketBooking");
        }
        TicketBooking existing = ticketBookingRepository.findById(ticketBooking.getId())
            .orElseThrow(
                () -> new IllegalArgumentException("Ticket booking with id=" + ticketBooking.getId()
                    + " does not exist. Nothing to update."));
        // only certain properties are eligible for update
        existing.setFlights(ticketBooking.getFlights());
        existing.setPassengers(ticketBooking.getPassengers());
        BigDecimal price = retrievePrice(ticketBooking);
        existing.setStatus(ticketBooking.getStatus());
        existing.setUpdatedAt(Instant.now());
        existing.setTotalPrice(price);

        TicketBooking updated = ticketBookingRepository.save(existing);
        log.info("Updated booking with id={}", ticketBooking.getId());
        bookingMessageSender.notifyForSavedBooking(updated);
        return updated;
    }

    @Cacheable("bookings")
    public Optional<TicketBooking> getBooking(@PathVariable String bookingId) {
        return ticketBookingRepository.findById(bookingId);
    }

    @CacheEvict(value = "bookings", key = "#id")
    public void deleteBooking(String bookingId) {
        ticketBookingRepository.deleteById(bookingId);
    }

    private BigDecimal retrievePrice(TicketBooking ticketBooking) {
        PricingRequest pricingRequest = objectMapper.convertValue(ticketBooking, PricingRequest.class);
        BigDecimal currentPrice = pricingServiceClient.getPrice(pricingRequest);
        log.debug("Fetched price={} for flight orig={} , dest={}, number of passengers={} ",
            currentPrice,
            pricingRequest.getOriginAirport(), pricingRequest.getDestinationAirport(),
            pricingRequest.getPassengers().size());
        return currentPrice;
    }
}
