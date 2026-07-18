package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.ChatMessage;
import com.javaee.backend.mapper.ChatMessageMapper;
import com.javaee.backend.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void save(String content, String chatId, Long userId, String role) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setMetadata(chatId);
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chatMessageMapper.insert(message);
    }
}