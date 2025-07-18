package com.luv2code.springboot.demo.mail;

import jakarta.mail.MessagingException;
import org.springframework.context.annotation.Bean;


public interface EmailSender {
    void sendSimpleMessage(String to, String from, String text);

    void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String filename, String username) throws MessagingException;
}
