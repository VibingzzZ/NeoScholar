package com.javaee.backend.service;

import com.javaee.backend.entity.StudentProfile;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 画像提取 AI 服务 — 每次对话完成后，后端主动用独立 AI 调用
 * 分析最新消息并提取画像字段，不依赖流式对话中 Tool 触发。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface ProfileExtractionService {

    @SystemMessage("""
            你是一个用户画像提取专家。分析用户的最新发言，提取以下信息。
            只返回一个严格的 JSON 对象，不包含任何解释文字。
            对于无法确定的信息，对应字段写入 null。

            返回格式：
            {
              "majorOrField": "专业或领域" | null,
              "learningGoal": "学习目标" | null,
              "knowledgeBase": "已有知识基础" | null,
              "cognitiveStyle": "思维风格" | null,
              "commonMistakes": "常见错误模式" | null,
              "interactionPreference": "交互偏好" | null
            }

            提取规则：
            - 只从用户发言中提取，不要从 AI 回复中提取
            - 明确的陈述才提取（如"我是计算机专业的" → majorOrField: "计算机专业"）
            - 模糊或隐含的信息不提取，设为 null
            """)
    @UserMessage("请从以下用户发言中提取画像信息：\n\n{{content}}")
    ProfileExtractionResult extract(String content);
}
