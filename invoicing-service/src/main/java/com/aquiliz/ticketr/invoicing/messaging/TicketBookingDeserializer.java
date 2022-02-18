package com.aquiliz.ticketr.invoicing.messaging;

import com.aquiliz.ticketr.invoicing.TicketBooking;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class TicketBookingDeserializer implements Deserializer<TicketBooking> {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public TicketBooking deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(new String(data), TicketBooking.class);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
