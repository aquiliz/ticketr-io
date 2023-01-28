package com.aquiliz.ticketr.invoicing.messaging;

import com.aquiliz.ticketr.invoicing.TicketBookingNotification;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class TicketBookingDeserializer implements Deserializer<TicketBookingNotification> {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());

    @Override
    public TicketBookingNotification deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(new String(data), TicketBookingNotification.class);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
