package com.aquiliz.ticketr.invoicing;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TicketBooking {
    private String id;
    private String userId;
    private String originAirport;
    private String destinationAirport;
    private String seat;
    private String seatClass;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timeOfBooking;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal price;
}
