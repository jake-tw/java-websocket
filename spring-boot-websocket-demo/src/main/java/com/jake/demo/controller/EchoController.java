package com.jake.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EchoController {

    @MessageMapping("/echo")
    @SendToUser(destinations = "/topic/echo", broadcast = false)
    public String echo(String text) {
        log.info("Echo: {}", text);
        return text;
    }
}
