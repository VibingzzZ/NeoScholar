package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_messages")
public class ChatMessage {
    private Long id;
    private Long userId;
    private String role;
    private String content;
    private String metadata;
    private Timestamp createdAt;

}
