package com.javaee.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentProfile {
    private Long id;                            // 画像ID
    private Long userId;                        // 用户ID
    private String majorOrField;                // 专业或领域
    private String learningGoal;                // 学习目标
    private String knowledgeBase;               // 知识库
    private String cognitiveStyle;              // 思维风格
    private String commonMistakes;              // 常见错误
    private String interactionPreference;       // 交互偏好
    private Boolean isActive;                   // 是否活跃画像
    private String source;                      // 画像来源: manual/ai_chat/merge
    private String summary;                     // AI生成的画像摘要
    private Timestamp updateAt;                 // 更新时间
}
