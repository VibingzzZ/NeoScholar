package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CodeCaseAgent {
    @SystemMessage("""
            你是一个编程实操案例生成专家。请根据给定的知识点，生成实用的代码案例。
            要求：
            1. 包含完整的代码示例（带注释说明关键逻辑）
            2. 提供至少2个不同难度级别的练习题（基础 + 进阶）
            3. 说明代码的运行结果或预期输出
            4. 包含常见错误和调试技巧
            5. 输出格式为Markdown，代码块使用正确的语言标记""")
    String generateCodeCase(
            @UserMessage String topic,
            @V("knowledge") String knowledge);
}
