package com.aquiliz.ticketr.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/book")
public class BookingController {

    private final TicketBookingRepository ticketBookingRepository;
    private final StreamBridge streamBridge;
    private final PricingServiceClient pricingServiceClient;
    public static final String PRODUCER_BINDING_NAME = "ticket-booking-out-0";


    public BookingController(TicketBookingRepository ticketBookingRepository, PricingServiceClient pricingServiceClient,
                             StreamBridge streamBridge) {
        this.ticketBookingRepository = ticketBookingRepository;
        this.pricingServiceClient = pricingServiceClient;
        this.streamBridge = streamBridge;
    }

    @PostMapping
    public ResponseEntity<String> bookTicket(@RequestBody TicketBooking ticketBooking) {
        BigDecimal currentPrice = pricingServiceClient.getPrice(ticketBooking.getUserId(), ticketBooking.getSeat(),
                ticketBooking.getOriginAirport(), ticketBooking.getDestinationAirport());
        ticketBooking.setPrice(currentPrice);

        TicketBooking saved = ticketBookingRepository.save(ticketBooking);
        sendMessage(saved);
        return ResponseEntity.ok().body(saved.getId());
    }

    private void sendMessage(TicketBooking saved) {
        boolean sent = streamBridge.send(PRODUCER_BINDING_NAME, saved);
        if (sent) {
            log.info("Successfully sent creation event for booking id={}", saved.getId());
        }
    }
}
