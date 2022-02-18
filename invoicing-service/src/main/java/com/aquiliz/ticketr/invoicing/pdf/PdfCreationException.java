package com.aquiliz.ticketr.invoicing.pdf;

public class PdfCreationException extends RuntimeException {
    public PdfCreationException(String message) {
        super(message);
    }

    public PdfCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
