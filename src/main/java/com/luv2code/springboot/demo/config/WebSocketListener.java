package com.luv2code.springboot.demo.config;

import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketListener {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    public static ArrayList<String> onlineUsers = new ArrayList<>();

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            System.out.printf("User disconnected: {" + username+"}");
            onlineUsers.remove(username);
            ChatMessage chatMessage = ChatMessage.builder()
                    .content("Leave")
                    .sender(username)
                    .build();

            simpMessagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/receive",
                    chatMessage
            );
        }
        else {
            System.out.println("Username equals zero");
        }
    }
}
