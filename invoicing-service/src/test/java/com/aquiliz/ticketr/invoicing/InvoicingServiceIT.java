package com.aquiliz.ticketr.invoicing;

import com.aquiliz.ticketr.invoicing.email.EmailService;
import com.aquiliz.ticketr.invoicing.messaging.TicketBookingConsumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvoicingServiceIT {

    @MockBean
    private EmailService emailService;
    @Autowired
    private TicketBookingConsumer ticketBookingConsumer;

    @Test
    public void shouldCreateInvoiceForBooking() {
		//GIVEN
        TicketBooking ticketBooking = buildBookingDto();

		//WHEN a new booking notification is received
        ticketBookingConsumer.accept(ticketBooking);

		//THEN email with the invoice pdf file should be sent out
        ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        Mockito.verify(emailService).sendInvoiceByEmail(captor.capture());
        File actualInvoice = captor.getValue();
        assertNotNull(actualInvoice);
        assertTrue(actualInvoice.getName().contains("invoice-" + ticketBooking.getUserId() + "-"));
        assertTrue(actualInvoice.getName().endsWith(".pdf"));
		//after sending out the email, the temp file should be deleted
        assertFalse(actualInvoice.exists());
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
}
