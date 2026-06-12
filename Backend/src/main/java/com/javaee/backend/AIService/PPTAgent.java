package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface PPTAgent {
    @SystemMessage("你是一个PPT生成专家，根据知识点生成结构化的PPT大纲...")
    String generateOutline(
            @UserMessage String topic,
            @V("knowledge")String knowledge);
}
