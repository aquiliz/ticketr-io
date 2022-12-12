package com.aquiliz.ticketr.booking;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class BookingService {
    private final TicketBookingRepository ticketBookingRepository;
    private final PricingServiceClient pricingServiceClient;
    private final BookingMessageSender bookingMessageSender;

    public BookingService(TicketBookingRepository ticketBookingRepository, PricingServiceClient pricingServiceClient,
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
        BigDecimal currentPrice = pricingServiceClient.getPrice(ticketBooking.getUserId(),
                ticketBooking.getSeat(),
                ticketBooking.getOriginAirport(), ticketBooking.getDestinationAirport());
        log.debug("Fetched price={} for flight orig={} , dest={}, seat={} ", currentPrice,
                ticketBooking.getOriginAirport(), ticketBooking.getDestinationAirport(), ticketBooking.getSeat());
        ticketBooking.setPrice(currentPrice);
        ticketBooking.setTimeOfBooking(Instant.now());

        TicketBooking saved = ticketBookingRepository.save(ticketBooking);
        log.info("Created new booking: id={} , orig={} , dest={}, seat={}", saved.getId(), saved.getOriginAirport(),
                saved.getDestinationAirport(), saved.getSeat());
        bookingMessageSender.notifyForSavedBooking(saved);
        return saved.getId();
    }

    public Optional<TicketBooking> getBooking(@PathVariable String bookingId) {
        return ticketBookingRepository.findById(bookingId);
    }

    public void deleteBooking(String bookingId) {
        ticketBookingRepository.deleteById(bookingId);
    }
}
