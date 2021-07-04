package com.jake.demo.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ChatMessage {

    private MessageType type;
    private String username;
    private String content;
    private LocalDateTime time;
}
