package com.javaee.backend.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Autowired
    private SparkApiAuthInterceptor sparkApiAuthInterceptor;

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return memoryId -> MessageWindowChatMemory.withMaxMessages(15);
    }

    /**
     * 为 LangChain4j 的 RestClient 注入星火 API HMAC 签名拦截器。
     * LangChain4j 内部使用 RestClient 调用 OpenAI 兼容接口，
     * 此 Customizer 会在所有 RestClient 请求中添加 HMAC-SHA256 签名头。
     */
    @Bean
    public RestClientCustomizer sparkAuthRestClientCustomizer() {
        return restClientBuilder -> restClientBuilder.requestInterceptor(sparkApiAuthInterceptor);
    }
}