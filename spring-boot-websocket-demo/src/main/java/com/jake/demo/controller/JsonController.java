package com.jake.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.jake.demo.model.ChatMessage;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class JsonController {

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        log.info("Json receive data: {}", chatMessage);
        return chatMessage;
    }
}
