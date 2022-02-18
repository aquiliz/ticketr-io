package com.aquiliz.ticketr.invoicing.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Value("${email-sending.smtp.host}")
    private String smtpHost;
    @Value("${email-sending.smtp.port}")
    private String smtpPort;
    @Value("${email-sending.smtp.username}")
    private String username;
    @Value("${email-sending.smtp.password}")
    private String password;
    private static final String EMAIL_FROM = "system@ticketr.io";
    //That's for demo purpose. In reality, this address would be retrieved from e.g. user service (another microservice)
    //or from the session, if available
    private static final String EMAIL_TO = "sample-user@example.com";


    public void sendInvoiceByEmail(File invoiceFile) {
        Message message = new MimeMessage(createSession());
        try {
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));
            message.setSubject("Your invoice from Ticketr.io");

            String msg = "Dear Ticketr.io user, please, find attached the invoice for your " +
                    "newly purchased plane ticket. We wish you a nice and pleasant flight!";
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.attachFile(invoiceFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            multipart.addBodyPart(attachmentBodyPart);
            message.setContent(multipart);

            Transport.send(message);
            log.info("Email was sent to receiver {} with attached invoice file {}", EMAIL_TO, invoiceFile.getName());
        } catch (MessagingException | IOException e) {
            throw new EmailSendingException("Failed to send invoice file '" + invoiceFile.getName() + " as an email", e);
        }

    }

    private Session createSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", smtpHost);
        prop.put("mail.smtp.port", smtpPort);
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}
