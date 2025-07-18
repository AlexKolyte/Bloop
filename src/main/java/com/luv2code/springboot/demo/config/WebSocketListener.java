package com.luv2code.springboot.demo.config;

import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketListener {
    private final SimpMessageSendingOperations messageTemplate;
    @EventListener
    public void EnableWebSocketDisconnectListener(SessionDisconnectEvent event){
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String username = (String) headerAccessor.getSessionAttributes().get("username");
//        if (username!=null){
//            log.info("user disconnected ", username);
//            var chatMessage = ChatMessage.builder().messageType(MessageType.LEAVE).sender(username).build();
//            messageTemplate.convertAndSendToUser(chatMessage.getRecipient(),"queue/receive", chatMessage);
//        }
    }
}
