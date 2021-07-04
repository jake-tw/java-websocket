package com.jake.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.jake.demo.model.ChatMessage;

@Controller
public class WebSocketController {

    @MessageMapping("/chat/new")
    @SendTo("/topic/chat")
    public ChatMessage newUser(@Payload ChatMessage chatMessage, StompHeaderAccessor stompHeaderAccessor) {
        stompHeaderAccessor.getSessionAttributes().put("username", chatMessage.getUsername());
        return chatMessage;
    }

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }
}
