package com.aquiliz.ticketr.booking;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketBookingRepository extends MongoRepository<TicketBooking, String> {
}
