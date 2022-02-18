package com.aquiliz.ticketr.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@Document
public class TicketBooking {
    @Id
    private String id;
    private String userId;
    private String originAirport;
    private String destinationAirport;
    private String seat;
    private String seatClass;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timeOfBooking;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Field(targetType = DECIMAL128)
    private BigDecimal price;
}
