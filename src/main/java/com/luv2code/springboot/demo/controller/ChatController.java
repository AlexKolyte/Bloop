package com.luv2code.springboot.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.config.WebSocketListener;
import com.luv2code.springboot.demo.db.DBManager;
import com.luv2code.springboot.demo.file.FileManager;
import com.luv2code.springboot.demo.mail.EmailSender;
import com.luv2code.springboot.demo.mail.EmailVerification;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    int count = 0;
    private final FileManager fileManager;

    public static ArrayList<ChatMessage> messages = new ArrayList<>();

    private final EmailSender emailSender;
    private final EmailVerification emailVerification;

    @Autowired
    public ChatController(EmailSender emailSender, EmailVerification emailVerification, FileManager fileManager) {
        this.emailSender = emailSender;
        this.emailVerification = emailVerification;
        this.fileManager = fileManager;
    }

    @MessageMapping("/chat.sendPrivateMessage/{recipient}")
    public void sendPrivateMessage(@DestinationVariable String recipient, @Payload ChatMessage message) {
        count++;
        if(count%2!=0) {
            DBManager.saveMessage(message);
        }
        simpMessagingTemplate.convertAndSendToUser(recipient, "/queue/receive", message);
    }

    @MessageMapping("/chat.sendOldMessage/{recipient}")
    public void sendOldMessage(@Payload ChatMessage chatMessage, @DestinationVariable String recipient) {
        simpMessagingTemplate.convertAndSendToUser(recipient, "/queue/receive", chatMessage);
    }

    @PostConstruct
    public void fillMessages() {
        DBManager.getAllMessages();
    }

    @GetMapping("/messages")
    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    @GetMapping("/messages/{user}")
    public ArrayList<ChatMessage> getMessagesFromUser(@PathVariable String user) {
        ArrayList<ChatMessage> user_messages = new ArrayList<>();
        for(int i =0; i<messages.size(); i++)
        {
           String sender_recipient = messages.get(i).getSender().toString()+ messages.get(i).getRecipient().toString();
           String recipient_sender = messages.get(i).getRecipient().toString()+ messages.get(i).getSender().toString();
            if(sender_recipient.equals(user)|| recipient_sender.equals(user))
            {
                user_messages.add(messages.get(i));
            }
        }
        return user_messages;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) throws MessagingException, URISyntaxException {

        WebSocketListener.onlineUsers.add(chatMessage.getSender());
//        String response = emailVerification.verifyEmail(chatMessage.getSenderEmail());
        String filePath = "/Users/alex/Downloads/Bloop/src/main/resources/static/image/icon.png";
        boolean check = false;

        headerAccessor.getSessionAttributes().putIfAbsent("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().putIfAbsent("password", chatMessage.getSenderPassword());
        headerAccessor.getSessionAttributes().putIfAbsent("email", chatMessage.getSenderEmail());

        for (int i = 0; i < UserRestController.users.size(); i++) {
            if (UserRestController.users.get(i).getUsername().equals(chatMessage.getSender())) {
                check = true;
            } else if (UserRestController.users.get(i).getEmail().equals(chatMessage.getSenderEmail())) {
                check = true;
            }
            if(UserRestController.users.get(i).getUsername().equals(chatMessage.getSender())&&!UserRestController.users.get(i).getEmail().equals(chatMessage.getSenderEmail()))
            {
                DBManager.updateUserEmail(UserRestController.users.get(i).getUsername(), chatMessage.getSenderEmail());
            }
        }
        if (check == false) {
            DBManager.createUser(chatMessage.getSender(), chatMessage.getSenderPassword(), chatMessage.getSenderEmail());
            DBManager.saveLastUser();
            return chatMessage;
        }

        emailSender.sendMessageWithAttachment(chatMessage.getSenderEmail(), "Hi "+ chatMessage.getSender() +". You have successfully created an account.", "Hi, We are Bloop.", filePath, fileManager.createPDF(chatMessage.getSender(), chatMessage.getSenderEmail()) ,  chatMessage.getSender());
        return null;
    }
}
