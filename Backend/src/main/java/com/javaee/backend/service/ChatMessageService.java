package com.javaee.backend.service;

import com.javaee.backend.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    void save(String content, String chatId, Long userId, String role);

    /** 查询某用户的所有聊天历史，按时间升序 */
    List<ChatMessage> getHistoryByUserId(Long userId);
}