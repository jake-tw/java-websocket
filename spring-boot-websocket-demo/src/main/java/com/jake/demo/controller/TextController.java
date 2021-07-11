package com.jake.demo.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TextController {

    @MessageMapping("/login")
    @SendToUser(destinations = "/topic/login", broadcast = false)
    public String login(String username, StompHeaderAccessor stompHeaderAccessor, Principal principal) {
        log.info("Text receive data: {}", username);
        stompHeaderAccessor.getSessionAttributes().put("username", username);
        log.info("Push message to: {}", principal.getName());
        return username;
    }
}
