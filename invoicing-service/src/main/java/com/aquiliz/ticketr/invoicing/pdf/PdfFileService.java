package com.aquiliz.ticketr.invoicing.pdf;

import com.aquiliz.ticketr.invoicing.TicketBookingNotification;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class PdfFileService {

    /**
     * Creates a pdf invoice file in the operating system's temp dir for the given {@link TicketBookingNotification}.
     * The generated file <b>has to be deleted after it has been successfully used / forwarded /b>.
     */
    public File createInvoiceDocument(TicketBookingNotification notification) {
        String invoiceId = UUID.randomUUID().toString();
        Document document = new Document();
        File tempFile = createTempFile(invoiceId, notification.getBookingId(), document);

        document.open();
        Font font26 = FontFactory.getFont(FontFactory.HELVETICA, 26, BaseColor.BLACK);
        Font font16 = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);

        Chunk title = new Chunk("Invoice num: " + invoiceId, font26);
        Chunk invoiceTo = new Chunk("Invoice to: " + notification.getCustomerFullName(), font16);
        Chunk activity = new Chunk(
            "Activity: Ticket booking #" + notification.getBookingId() + " flight(s) from "
                + notification.getOriginAirport() + " to " + notification.getDestinationAirport(),
            font16);
        Chunk time = new Chunk("Booking time: " + notification.getTimeOfBooking(), font16);
        Chunk price = new Chunk("Amount: $" + notification.getTotalPrice(), font16);

        Paragraph titleParagraph = new Paragraph(title);
        Paragraph text = new Paragraph();
        text.add(invoiceTo);
        text.add(Chunk.NEWLINE);
        text.add(activity);
        text.add(Chunk.NEWLINE);
        text.add(time);
        text.add(Chunk.NEWLINE);
        text.add(price);

        try {
            document.add(titleParagraph);
            document.add(Chunk.NEWLINE);
            document.add(text);
        } catch (DocumentException e) {
            throw new PdfCreationException("Failed to create pdf invoice file for ticket booking id=" + notification, e);
        }

        document.close();
        log.info("Generated invoice id={} for ticket booking id={}", invoiceId, notification.getBookingId());
        return tempFile;
    }

    private File createTempFile(String invoiceId, String bookingId, Document document) {
        File tempFile;
        try {
            tempFile = File.createTempFile("invoice-" + invoiceId + "-", ".pdf");
            PdfWriter.getInstance(document, new FileOutputStream(tempFile));
        } catch (DocumentException | IOException e) {
            throw new PdfCreationException(
                "Failed to create pdf file for invoiceId= " + invoiceId
                    + " ticket booking id=" + bookingId, e);
        }
        return tempFile;
    }

}
