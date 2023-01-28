package com.aquiliz.ticketr.booking;

import com.aquiliz.ticketr.booking.dto.TicketBooking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketBookingRepository extends MongoRepository<TicketBooking, String> {
}
