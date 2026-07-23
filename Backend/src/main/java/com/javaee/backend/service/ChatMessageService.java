package com.javaee.backend.service;

import com.javaee.backend.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    void save(String content, String chatId, Long userId, String role);

    /** 查询某用户的所有聊天历史，按时间升序 */
    List<ChatMessage> getHistoryByUserId(Long userId);

    /** 获取用户最近N条消息，用于画像提取 */
    List<ChatMessage> getRecentMessages(Long userId, int limit);
}