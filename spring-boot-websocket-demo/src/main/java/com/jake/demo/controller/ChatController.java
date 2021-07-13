package com.jake.demo.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.jake.demo.model.ChatMessage;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ChatController {

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, StompHeaderAccessor stompHeaderAccessor,
            Principal principal) {
        log.info("[{}] Receive message: {}", principal.getName(), chatMessage);
        stompHeaderAccessor.getSessionAttributes().put("username", chatMessage.getUsername());
        return chatMessage;
    }
}
