package com.telnet.leaveapp.telnetleavemanager.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MailingService {

//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//
//    private final JavaMailSender javaMailSender;
//
//    public String sendMail( String to, String subject, String body) {
//        try {
//            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//
//            mimeMessageHelper.setFrom(fromEmail);
//            mimeMessageHelper.setTo(to);
//            mimeMessageHelper.setSubject(subject);
//            mimeMessageHelper.setText(body);
//
//            javaMailSender.send(mimeMessage);
//
//            return "mail sent successfully";
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender javaMailSender;

    @Async
    public CompletableFuture<String> sendMailAsync(String to, String subject, String body) {
        return sendMail(to, subject, body);
    }

    @Async
    public CompletableFuture<String> sendMail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(fromEmail);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body);

            javaMailSender.send(mimeMessage);

            return CompletableFuture.completedFuture("Mail sent successfully");

        } catch (MessagingException e) {
            return CompletableFuture.failedFuture(new RuntimeException("Error sending email", e));
        }
    }
}
