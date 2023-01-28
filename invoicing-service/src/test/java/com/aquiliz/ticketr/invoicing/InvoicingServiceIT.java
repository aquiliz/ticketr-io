package com.aquiliz.ticketr.invoicing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.aquiliz.ticketr.invoicing.email.EmailService;
import com.aquiliz.ticketr.invoicing.messaging.TicketBookingConsumer;
import com.aquiliz.ticketr.invoicing.pdf.PdfFileService;
import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvoicingServiceIT {

    @MockBean
    private EmailService emailService;
    @SpyBean
    private PdfFileService pdfFileService;
    @Autowired
    private TicketBookingConsumer ticketBookingConsumer;

    @Test
    public void shouldCreateInvoiceForBooking() {
		//GIVEN
        TicketBookingNotification ticketBookingNotification = buildBookingNotification();

		//WHEN a new booking notification is received
        ticketBookingConsumer.accept(ticketBookingNotification);

        //THEN pdf file creation should be initiated with the newly arrived booking notification
        verify(pdfFileService).createInvoiceDocument(eq(ticketBookingNotification));

        //THEN email with the invoice pdf file should be sent out
        ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        verify(emailService).sendInvoiceByEmail(captor.capture());
        File actualInvoice = captor.getValue();
        assertNotNull(actualInvoice);
        assertTrue(actualInvoice.getName().contains("invoice-"));
        assertTrue(actualInvoice.getName().endsWith(".pdf"));
        //after sending out the email, the temp file should be deleted
        assertFalse(actualInvoice.exists());
    }

    private TicketBookingNotification buildBookingNotification() {
        TicketBookingNotification notification = new TicketBookingNotification();
        notification.setBookingId("booking1234");
        notification.setOriginAirport("SYD");
        notification.setDestinationAirport("KUL");
        notification.setTotalPrice(BigDecimal.valueOf(1200.12));
        notification.setTimeOfBooking(Instant.now());
        notification.setCustomerFullName("John Doe");
        return notification;
    }
}
