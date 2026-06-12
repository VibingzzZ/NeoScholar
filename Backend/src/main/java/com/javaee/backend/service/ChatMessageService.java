package com.javaee.backend.service;

public interface ChatMessageService {
    void save(String content, String chatId, Long userId, String role);
}