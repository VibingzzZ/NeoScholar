package com.javaee.backend.entity;

import dev.langchain4j.internal.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentProfile {
    private Long id;                            // 学生id
    private Long userId;                        // 用户id
    private String majorOrField;                // 专业或领域
    private Text learningGoal;                  // 学习目标
    private Text knowledgeBase;                 // 知识库
    private String cognitiveStyle;              // 思维风格
    private Json commonMistakes;                // 常见错误
    private String interactionPreference;       // 交互偏好
    private Timestamp updateAt;                 // 更新时间

}
