package com.aquiliz.ticketr.invoicing.messaging;

import com.aquiliz.ticketr.invoicing.TicketBookingNotification;
import com.aquiliz.ticketr.invoicing.email.EmailService;
import com.aquiliz.ticketr.invoicing.pdf.PdfFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Consumer;

@Component("ticketBookingConsumer")
@Slf4j
public class TicketBookingConsumer implements Consumer<TicketBookingNotification> {

    @Value("${email-sending.enable}")
    private boolean enableEmailSending;
    private final PdfFileService pdfFileService;
    private final EmailService emailService;

    public TicketBookingConsumer(PdfFileService pdfFileService, EmailService emailService) {
        this.pdfFileService = pdfFileService;
        this.emailService = emailService;
    }

    @Override
    public void accept(TicketBookingNotification ticketBookingNotification) {
        log.info("Received new ticket booking: {}", ticketBookingNotification);
        File invoiceDocument = pdfFileService.createInvoiceDocument(ticketBookingNotification);
        if (enableEmailSending) {
            emailService.sendInvoiceByEmail(invoiceDocument);
            boolean deleted = invoiceDocument.delete();
            if (deleted) {
                log.info("Deleted invoice file {}", invoiceDocument.getName());
                return;
            } else {
                log.warn("Failed to delete generated invoice file {}", invoiceDocument.getName());
            }
        }
        invoiceDocument.deleteOnExit();
    }
}
