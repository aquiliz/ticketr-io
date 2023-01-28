package com.aquiliz.ticketr.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class PricingRequest {
    private String originAirport;
    private String destinationAirport;
    private List<Flight> flights;
    private List<Passenger> passengers;
    private String voucherNumber;

    @Data
    public static class Passenger {
        private List<PricingRequest.BookedSeat> bookedSeats;
        private int piecesOfCheckedBaggage;
    }

    @Data
    @Document
    public static class BookedSeat {
        private String seatNumber;
        private PricingRequest.SeatClass seatClass;
        private String flightNumber;
    }

    @Data
    public static class Flight {

        private String flightNumber;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
        private Instant scheduledTime;
        private String flightOrigin;
        private String flightDestination;

        @Override
        public String toString() {
            return flightNumber;
        }
    }

    public enum SeatClass {
        ECONOMY, BUSINESS, FIRST;
    }
}
