package com.javaee.backend.po.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String question;
    private String chatId;
    private Long userId;
}
