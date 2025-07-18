package com.luv2code.springboot.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Set destination prefix for application (sending messages from clients)
        registry.setApplicationDestinationPrefixes("/app");

        // Enable simple broker for broadcasting messages
        registry.enableSimpleBroker("/topic", "/user"); // Enable private user destinations

        // Configure the user-specific destination prefix
        registry.setUserDestinationPrefix("/user");
    }
}
