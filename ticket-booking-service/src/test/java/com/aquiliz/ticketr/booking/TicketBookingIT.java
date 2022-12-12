package com.aquiliz.ticketr.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TicketBookingIT {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
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

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void shouldSaveBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        withExistingSeatPrice("25A", 3.1415);

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
        assertEquals(BigDecimal.valueOf(3.1415), actualBooking.getPrice());
        actualBooking.setId(null);
        actualBooking.setPrice(null);
        actualBooking.setTimeOfBooking(null);
        assertEquals(ticketBooking, actualBooking);
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        //GIVEN
        TicketBooking ticketBooking = buildBookingDto();
        withExistingSeatPrice("25A", 3.1415);

        //WHEN a new booking is created
        MvcResult mvcResult = doPost("/" + BOOKING_PATH, ticketBooking);
        String bookingId = mvcResult.getResponse().getContentAsString();
        assertNotNull(bookingId);

        //WHEN the new booking is deleted
        doDelete("/" + BOOKING_PATH + "/" + bookingId);

        //THEN trying to retrieve that booking should return 404
        doGetAndExpectNotFound("/" + BOOKING_PATH + "/" + bookingId);
    }

    private void withExistingSeatPrice(String seatNumber, double price) {
        when(pricingServiceClient.getPrice(anyString(), eq(seatNumber), anyString(),
                anyString())).thenReturn(BigDecimal.valueOf(price));
    }

    public MvcResult doPost(String url, Object body) throws Exception {
        return this.mockMvc.perform(
                        post(url).content(asJsonString(body)).contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn();
    }

    public void doDelete(String url) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(url)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    private TicketBooking buildBookingDto() {
        TicketBooking ticketBooking = new TicketBooking();
        ticketBooking.setOriginAirport("SYD");
        ticketBooking.setDestinationAirport("KUL");
        ticketBooking.setSeat("25A");
        ticketBooking.setUserId("user1234");
        ticketBooking.setSeatClass("1");
        return ticketBooking;
    }

    public MvcResult doGet(String url) throws Exception {
        return this.mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn();
    }

    public void doGetAndExpectNotFound(String url) throws Exception {
        this.mockMvc.perform(get(url)).andExpect(status().isNotFound());
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
