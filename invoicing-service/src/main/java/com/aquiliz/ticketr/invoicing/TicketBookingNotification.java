package com.aquiliz.ticketr.invoicing;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class TicketBookingNotification {

  private String bookingId;
  private String originAirport;
  private String destinationAirport;
  private String customerFullName;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal totalPrice;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
  private Instant timeOfBooking;
}
