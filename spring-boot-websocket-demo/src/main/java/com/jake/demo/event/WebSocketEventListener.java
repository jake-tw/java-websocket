package com.jake.demo.event;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.jake.demo.model.ChatMessage;
import com.jake.demo.model.MessageType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;

    @EventListener
    public void handleConnectEventListener(SessionConnectEvent event) {
        log.info("New user connected.");
    }

    @EventListener
    public void handleDisconnectEventListener(SessionDisconnectEvent event) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) stompHeaderAccessor.getSessionAttributes().get("username");

        ChatMessage message = ChatMessage.builder().username(username).content("goodbye").time(LocalDateTime.now())
                .type(MessageType.DISCONNECT).build();

        simpMessageSendingOperations.convertAndSend("/topic/chat", message);
        log.info("User disconnected.");
    }
}
