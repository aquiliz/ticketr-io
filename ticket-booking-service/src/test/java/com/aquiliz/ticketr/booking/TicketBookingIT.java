package com.aquiliz.ticketr.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aquiliz.ticketr.booking.dto.PricingRequest;
import com.aquiliz.ticketr.booking.dto.TicketBooking;
import com.aquiliz.ticketr.booking.dto.TicketBooking.BookedSeat;
import com.aquiliz.ticketr.booking.dto.TicketBooking.SeatClass;
import com.aquiliz.ticketr.booking.dto.TicketBooking.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TicketBookingIT {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:latest"));
    private static final String BOOKING_PATH = "booking";
    @MockBean
    private BookingMessageSender bookingMessageSender;
    @MockBean
    private PricingServiceClient pricingServiceClient;
    @Autowired
    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void shouldCreateNewBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        mockPrice(3.1415);

        //WHEN a new booking is created
        MvcResult mvcResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = mvcResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //THEN it should be retrievable via the API
        MvcResult retrievedResult = doGet("/" + BOOKING_PATH + "/" + bookingId);
        TicketBooking actualBooking = objectMapper.readValue(
            retrievedResult.getResponse().getContentAsString(), TicketBooking.class);
        assertNotNull(actualBooking);

        //THEN its fields should be populated with the expected values
        assertNotNull(actualBooking.getId());
        assertNotNull(actualBooking.getTimeOfBooking());
        assertEquals(BigDecimal.valueOf(3.1415), actualBooking.getTotalPrice());
        //since these are dynamically generated, they can't be simply asserted with equals
        actualBooking.setId(null);
        actualBooking.setTotalPrice(null);
        actualBooking.setTimeOfBooking(null);
        assertEquals(ticketBooking, actualBooking);
    }

    @Test
    void shouldUpdateExistingBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        mockPrice(3.1415);

        //WHEN a new booking is created
        MvcResult mvcResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = mvcResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //WHEN the existing booking is updated
        ticketBooking.setId(bookingId);
        ticketBooking.setStatus(Status.FULFILLED);
        TicketBooking.BookedSeat bookedSeat1 = new BookedSeat();
        bookedSeat1.setSeatNumber("25B");
        bookedSeat1.setFlightNumber("TK3030");
        bookedSeat1.setSeatClass(SeatClass.ECONOMY);
        TicketBooking.Passenger additionalPassenger = new TicketBooking.Passenger();
        additionalPassenger.setPassportNumber("1111");
        additionalPassenger.setFirstName("Jonathan");
        additionalPassenger.setLastName("Dowsen");
        additionalPassenger.setBookedSeats(List.of(bookedSeat1));
        ticketBooking.getPassengers().add(additionalPassenger);

        ticketBooking.getFlights().stream()
            .filter(flight -> flight.getFlightNumber().equals("TK4040")).findFirst()
            .ifPresent(flight -> {
                flight.setFlightNumber("TK5050");
                flight.setFlightDestination("IST");
            });
        doPut("/" + BOOKING_PATH, ticketBooking);

        //THEN it should be retrievable via the API
        MvcResult retrievedResult = doGet("/" + BOOKING_PATH + "/" + bookingId);
        TicketBooking actualBooking = objectMapper.readValue(
            retrievedResult.getResponse().getContentAsString(), TicketBooking.class);
        assertNotNull(actualBooking);

        //THEN its fields should be updated with the expected values
        assertNotNull(actualBooking.getTimeOfBooking());
        assertNotNull(actualBooking.getUpdatedAt());
        assertEquals(BigDecimal.valueOf(3.1415), actualBooking.getTotalPrice());
        //since these are dynamically generated, they can't be simply asserted with equals
        actualBooking.setTotalPrice(null);
        actualBooking.setTimeOfBooking(null);
        actualBooking.setUpdatedAt(null);
        assertEquals(ticketBooking, actualBooking);
    }

    @Test
    void shouldThrowException_IfMissingId_UpdateBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        mockPrice(3.1415);

        //WHEN a new booking is created
        MvcResult postResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = postResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //WHEN an update attempt is made with a null id
        ticketBooking.setStatus(Status.FULFILLED);

        //THEN the request should return status code 500 with the appropriate error message
        MvcResult putResult = this.mockMvc.perform(
                put("/" + BOOKING_PATH).content(asJsonString(ticketBooking))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError())
            .andReturn();
        String responseMessage = putResult.getResponse().getContentAsString();
        assertEquals("ID is required in order to update ticketBooking", responseMessage);
    }

    @Test
    void shouldThrowException_attemptToUpdate_NonExisting_Booking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        mockPrice(3.1415);

        //WHEN a new booking is created
        MvcResult postResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = postResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //WHEN an update attempt is made with an id which does not exist in db
        ticketBooking.setId("123456");
        ticketBooking.setStatus(Status.FULFILLED);

        //THEN the request should return status code 500 with the appropriate error message
        MvcResult putResult = this.mockMvc.perform(
                put("/" + BOOKING_PATH).content(asJsonString(ticketBooking))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError())
            .andReturn();
        String responseMessage = putResult.getResponse().getContentAsString();
        assertEquals("Ticket booking with id=123456 does not exist. Nothing to update.",
            responseMessage);
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        mockPrice(3.1415);

        //WHEN a new booking is created
        MvcResult mvcResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = mvcResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //WHEN the new booking is deleted
        doDelete("/" + BOOKING_PATH + "/" + bookingId);

        //THEN trying to retrieve that booking should return 404
        doGetAndExpectNotFound("/" + BOOKING_PATH + "/" + bookingId);
    }

    private void mockPrice(double price) {
        when(pricingServiceClient.getPrice(any(PricingRequest.class))).thenReturn(
            BigDecimal.valueOf(price));
    }

    public MvcResult doPost(String url, Object body) throws Exception {
        return this.mockMvc.perform(
                        post(url).content(asJsonString(body)).contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn();
    }

    public MvcResult doPut(String url, Object body) throws Exception {
        return this.mockMvc.perform(
                put(url).content(asJsonString(body)).contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andReturn();
    }

    public void doDelete(String url) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(url)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    public MvcResult doGet(String url) throws Exception {
        return this.mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn();
    }

    public void doGetAndExpectNotFound(String url) throws Exception {
        this.mockMvc.perform(get(url)).andExpect(status().isNotFound());
    }

    private TicketBooking buildBookingDto() {

        TicketBooking.Flight flight1 = new TicketBooking.Flight();
        flight1.setFlightNumber("TK3030");
        flight1.setFlightOrigin("SYD");
        flight1.setFlightDestination("KUL");
        flight1.setScheduledTime(
            Instant.now().plus(22, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES));

        TicketBooking.Flight flight2 = new TicketBooking.Flight();
        flight2.setFlightNumber("TK4040");
        flight2.setFlightOrigin("KUL");
        flight2.setFlightDestination("DOH");
        flight2.setScheduledTime(
            Instant.now().plus(23, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES));

        BookedSeat bookedSeat1 = new BookedSeat();
        bookedSeat1.setSeatNumber("25A");
        bookedSeat1.setFlightNumber("TK3030");
        bookedSeat1.setSeatClass(SeatClass.ECONOMY);

        BookedSeat bookedSeat2 = new BookedSeat();
        bookedSeat2.setSeatNumber("21C");
        bookedSeat2.setFlightNumber("TK4040");
        bookedSeat2.setSeatClass(SeatClass.ECONOMY);

        TicketBooking.Passenger passenger1 = new TicketBooking.Passenger();
        passenger1.setPassportNumber("123123");
        passenger1.setFirstName("John");
        passenger1.setLastName("Doe");
        passenger1.setBookedSeats(List.of(bookedSeat1, bookedSeat2));
        passenger1.setPiecesOfCheckedBaggage(1);

        TicketBooking ticketBooking = new TicketBooking();
        ticketBooking.setUserId("user1234");
        ticketBooking.setOriginAirport("SYD");
        ticketBooking.setDestinationAirport("DOH");
        List<TicketBooking.Passenger> passengers = new ArrayList<>();
        passengers.add(passenger1);
        ticketBooking.setPassengers(passengers);
        ticketBooking.setFlights(List.of(flight1, flight2));
        ticketBooking.setVoucherNumber("1234");
        ticketBooking.setStatus(Status.ACTIVE);

        return ticketBooking;
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
