package com.javaee.backend.AIService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 提供用户对话服务
 */
@AiService(tools = {"GenerateTools", "ProfileExtractionTools", "CreatePathTools"})
public interface ConsultantService {
    /**
     * 流式输出
     * @param question  用户输入
     * @param chatId    对话ID
     * @return          大模型输出流
     */
    @SystemMessage(fromResource = "SystemPrompt.txt")
    Flux<String> chatWithStream(@UserMessage String question, @MemoryId String chatId);

}