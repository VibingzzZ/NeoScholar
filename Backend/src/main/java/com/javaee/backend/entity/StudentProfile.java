package com.javaee.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentProfile {
    private Long id;                            // 学生id
    private Long userId;                        // 用户id
    private String majorOrField;                // 专业或领域
    private String learningGoal;                // 学习目标
    private String knowledgeBase;               // 知识库
    private String cognitiveStyle;              // 思维风格
    private String commonMistakes;              // 常见错误（JSON字符串）
    private String interactionPreference;       // 交互偏好
    private Timestamp updateAt;                 // 更新时间

}