package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface PPTAgent {

    @SystemMessage("""
            你是一个PPT生成专家。根据给定的知识点和学习内容，生成一份结构化的 PPT 大纲。
            每页包含标题和要点，共 5-8 页。用 Markdown 格式输出，结构清晰。""")
    @UserMessage("知识点：{{topic}}\n学习内容：{{description}}")
    String generateOutline(
            @V("topic") String topic,
            @V("description") String description);
}
