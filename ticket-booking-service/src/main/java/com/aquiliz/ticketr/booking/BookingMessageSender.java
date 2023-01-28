package com.aquiliz.ticketr.booking;

import com.aquiliz.ticketr.booking.dto.TicketBooking;
import com.aquiliz.ticketr.booking.dto.TicketBooking.Passenger;
import com.aquiliz.ticketr.booking.dto.TicketBookingNotification;
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

    public void notifyForSavedBooking(TicketBooking ticketBooking) {
        boolean sent = streamBridge.send(PRODUCER_BINDING_NAME, createBookingNotification(ticketBooking));
        if (sent) {
            log.info("Successfully sent creation event for booking id={}", ticketBooking.getId());
        }
    }

    private TicketBookingNotification createBookingNotification(TicketBooking ticketBooking) {
        TicketBooking.Passenger billingPerson = ticketBooking.getPassengers().stream()
            .filter(Passenger::isBillingPerson).findFirst().orElseThrow(
                () -> new IllegalArgumentException(
                    "Failed to find a billing person among ticket's passengers"));

        TicketBookingNotification notification = new TicketBookingNotification();
        notification.setBookingId(ticketBooking.getId());
        notification.setOriginAirport(ticketBooking.getOriginAirport());
        notification.setDestinationAirport(ticketBooking.getDestinationAirport());
        notification.setCustomerFullName(
            billingPerson.getFirstName() + " " + billingPerson.getLastName());
        notification.setTotalPrice(ticketBooking.getTotalPrice());
        notification.setTimeOfBooking(ticketBooking.getTimeOfBooking());
        return notification;
    }
}
