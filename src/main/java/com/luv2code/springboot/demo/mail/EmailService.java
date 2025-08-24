package com.luv2code.springboot.demo.mail;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.DataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EmailService implements EmailSender, EmailVerification{
    @Value("${emailListVerify.apiKey}")
    private String apiKey;

    @Value("${emailListVerify.apiUrl}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final JavaMailSender javaMailSender;
    public EmailService(RestTemplate restTemplate, JavaMailSender javaMailSender) throws MessagingException {
        this.restTemplate = restTemplate;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text)
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sancho.pancho1704@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String filename, String username) throws MessagingException, MailSendException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("sancho.pancho1704@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);


        if (pathToAttachment != null && !pathToAttachment.isEmpty()) {
            ImageIcon image = new ImageIcon();
            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment("Bloop", file, "image/png");
        }

        MimeBodyPart pdfPart = new MimeBodyPart();
        FileDataSource source = new FileDataSource(filename);
        pdfPart.setDataHandler(new DataHandler((jakarta.activation.DataSource) source));
        pdfPart.setFileName("User:" + username + ".pdf");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(pdfPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        String orderContent = "<head>\n" +
                "<style>\n" +
                "#items {\n" +
                "  font-family: Arial, Helvetica, sans-serif;\n" +
                "  border-collapse: collapse;\n" +
                "  width: 100%;\n" +
                "}\n" +
                "\n" +
                "#items td, #customers th {\n" +
                "  border: 1px solid #ddd;\n" +
                "  padding: 8px;\n" +
                "}\n" +
                "\n" +
                "#items tr:nth-child(odd){background-color: #FFFFFF;}\n" +
                "#items tr:nth-child(even){background-color: #f2f2f2;}\n" +
                "\n" +
                "#items tr:hover {background-color: #ddd;}\n" +
                "\n" +
                "#items th {\n" +
                "  padding-top: 12px;\n" +
                "  padding-bottom: 12px;\n" +
                "  text-align: left;\n" +
                "  background-color: #04AA6D;\n" +
                "  color: white;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h3>Recipient: " + username + "</h3>\n" +
                "<h3>Адреса доставки:  " + to + "</h3>\n" +
                "<h3>Date: " + LocalDateTime.now().toLocalDate().toString() + "</h3>\n" +
                "\n"+
                "<img src='/Users/alex/Downloads/Bloop/src/main/resources/static/image/icon.png' alt='Bloop image' width='300' height='200'>\n" +  // Image added here
                "\n";

        htmlPart.setContent(orderContent, "text/html; charset=utf-8");
        multipart.addBodyPart(htmlPart);

        message.setContent(multipart);

        try {
            javaMailSender.send(message);
        }catch (MailSendException mailSendException){
            System.out.println("No email exists");
        }
    }

    @Override
    public String verifyEmail(String email) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.emaillistverify.com")
                .path("/api/verifyEmail")
                .queryParam("email", email)
                .queryParam("apikey", apiKey);

        // Get the final URI string
        String url = builder.toUriString();

        // Make the API call and return the response
        return restTemplate.getForObject(url, String.class);
    }

}
