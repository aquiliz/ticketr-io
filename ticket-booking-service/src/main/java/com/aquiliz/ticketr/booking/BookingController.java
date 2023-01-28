package com.aquiliz.ticketr.booking;

import com.aquiliz.ticketr.booking.dto.TicketBooking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> bookTicket(@RequestBody TicketBooking ticketBooking) {
        String savedBookingId = bookingService.createBooking(ticketBooking);
        return ResponseEntity.ok().body(savedBookingId);
    }

    @PutMapping
    public ResponseEntity<TicketBooking> updateTicket(@RequestBody TicketBooking ticketBooking) {
        TicketBooking updatedBooking = bookingService.updateBooking(ticketBooking);
        return ResponseEntity.ok().body(updatedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable String bookingId) {
        Optional<TicketBooking> booking = bookingService.getBooking(bookingId);
        return booking.<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Object> deleteBooking(@PathVariable String bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}
