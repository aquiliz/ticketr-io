package com.aquiliz.ticketr.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingMessageSender {
    private final StreamBridge streamBridge;
    public static final String PRODUCER_BINDING_NAME = "ticket-booking-out-0";

    public BookingMessageSender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void notifyForSavedBooking(TicketBooking saved) {
        boolean sent = streamBridge.send(PRODUCER_BINDING_NAME, saved);
        if (sent) {
            log.info("Successfully sent creation event for booking id={}", saved.getId());
        }
    }
}
