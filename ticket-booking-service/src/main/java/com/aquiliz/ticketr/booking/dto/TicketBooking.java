package com.aquiliz.ticketr.booking.dto;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document
public class TicketBooking {

    @Id
    private String id;
    private String userId;
    private String originAirport;
    private String destinationAirport;
    private Status status;
    private List<Flight> flights;
    private List<Passenger> passengers;
    private String voucherNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Field(targetType = DECIMAL128)
    private BigDecimal totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timeOfBooking;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    @Data
    @Document
    public static class Passenger {
        @Id
        private String passportNumber;
        private String firstName;
        private String lastName;
        private List<BookedSeat> bookedSeats;
        private boolean billingPerson;
        private int piecesOfCheckedBaggage;
        private boolean vegetarianMeal;
        private boolean specialNeeds;
        private String specialNeedsDescription;
    }

    @Data
    @Document
    public static class BookedSeat {
        private String seatNumber;
        private SeatClass seatClass;
        private String flightNumber;
    }

    @Data
    @Document
    public static class Flight {

        @Id
        private String flightNumber;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
        private Instant scheduledTime;
        private String flightOrigin;
        private String flightDestination;
    }

    public enum SeatClass {
        ECONOMY, BUSINESS, FIRST;
    }

    public enum Status {
        ACTIVE, CANCELLED, FULFILLED;
    }
}
