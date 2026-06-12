package com.javaee.backend.AIService;

import dev.langchain4j.service.spring.AiService;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface QuizAgent {
    @SystemMessage("你是一个出题专家，根据知识点生成选择题和简答题...")
    String generateQuestion(
            @UserMessage String topic,
            @V("knowledge") String knowledge);
}
