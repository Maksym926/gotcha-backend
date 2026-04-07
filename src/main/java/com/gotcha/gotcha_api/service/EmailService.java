package com.gotcha.gotcha_api.service;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Slf4j
@Service
public class EmailService {

    @Value("${gmail.client.id}")
    private String clientId;

    @Value("${gmail.client.secret}")
    private String clientSecret;

    @Value("${gmail.refresh.token}")
    private String refreshToken;

    @Value("${gmail.sender.address}")
    private String senderEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private Gmail gmailService;



    @PostConstruct
    public void initGmailService() {
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        this.gmailService = new Gmail.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName("Gotcha Cafe Mailer")
                .build();
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Gotcha Cafe - Password Reset Request";
        String body = "Hello,\n\n" +
                "You have requested to reset your password.\n\n" +
                "Click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "- Gotcha Cafe Team";

        sendEmailViaGoogleApi(toEmail, subject, body, "password reset");
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        String subject = "Gotcha Cafe - Verify Your Email"; // Fixed the copy-paste bug here!
        String body = "Hello,\n\n" +
                "Welcome to Gotcha Cafe!\n\n" +
                "Please verify your email by clicking the link below:\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "- Gotcha Cafe Team";

        sendEmailViaGoogleApi(toEmail, subject, body, "verification");
    }

    // --- Private helper method that handles all the Google API & Jakarta Mail boilerplate ---
    private void sendEmailViaGoogleApi(String toEmail, String subject, String bodyText, String emailContext) {
        try {
            // 1. Construct the Email using Jakarta Mail
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);

            email.setFrom(new InternetAddress(senderEmail));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
            email.setSubject(subject);
            email.setText(bodyText);

            // 2. Encode the Email to Base64 (Google API Requirement)
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

            // 3. Create the Google API payload and send
            Message message = new Message();
            message.setRaw(encodedEmail);

            gmailService.users().messages().send("me", message).execute();
            log.info("{} email sent successfully to: {}", emailContext, toEmail);

        } catch (MessagingException | IOException e) {
            log.error("Failed to send {} email to: {}", emailContext, toEmail, e);
            throw new RuntimeException("Failed to send " + emailContext + " email", e);
        }
    }
}