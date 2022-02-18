package com.aquiliz.ticketr.invoicing.pdf;

import com.aquiliz.ticketr.invoicing.TicketBooking;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class PdfFileService {

    /**
     * Creates a pdf invoice file in the operating system's temp dir for the given {@link TicketBooking}.
     * The generated file <b>has to be deleted after it has been successfully used / forwarded /b>.
     */
    public File createInvoiceDocument(TicketBooking ticketBooking) {
        Document document = new Document();
        File tempFile = createTempFile(ticketBooking, document);

        document.open();
        Font font36 = FontFactory.getFont(FontFactory.HELVETICA, 36, BaseColor.BLACK);
        Font font16 = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);

        Chunk title = new Chunk("Invoice", font36);
        Chunk details = new Chunk("UserId: " + ticketBooking.getUserId(), font16);
        Chunk price = new Chunk("Price: " + ticketBooking.getPrice(), font16);

        Paragraph titleParagraph = new Paragraph(title);
        Paragraph text = new Paragraph();
        text.add(details);
        text.add(Chunk.NEWLINE);
        text.add(price);
        try {
            document.add(titleParagraph);
            document.add(Chunk.NEWLINE);
            document.add(text);
        } catch (DocumentException e) {
            throw new PdfCreationException("Failed to create pdf invoice file for ticket booking id=" + ticketBooking, e);
        }

        document.close();
        log.info("Generated invoice document for userId={} , fileName={}", ticketBooking.getUserId(), tempFile.getName());
        return tempFile;
    }

    private File createTempFile(TicketBooking ticketBooking, Document document) {
        File tempFile;
        try {
            tempFile = File.createTempFile("invoice-" + ticketBooking.getUserId() + "-", ".pdf");
            PdfWriter.getInstance(document, new FileOutputStream(tempFile));
        } catch (DocumentException | IOException e) {
            throw new PdfCreationException("Failed to create pdf invoice file for ticket booking id=" + ticketBooking, e);
        }
        return tempFile;
    }

}
