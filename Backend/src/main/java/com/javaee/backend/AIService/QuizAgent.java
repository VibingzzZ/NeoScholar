package com.javaee.backend.AIService;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface QuizAgent {

    @SystemMessage("""
            你是一个出题专家。根据给定的知识点和学习内容，生成 3-5 道练习题。
            题目类型包括选择题（4个选项，标注正确答案）和简答题。
            用 Markdown 格式输出，结构清晰。""")
    @UserMessage("知识点：{{topic}}\n学习内容：{{description}}")
    String generateQuestion(
            @V("topic") String topic,
            @V("description") String description);
}
