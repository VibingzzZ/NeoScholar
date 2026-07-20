package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ReadingMaterialAgent {
    @SystemMessage("""
            你是一个拓展阅读材料生成专家。请根据给定的知识点，生成高质量的拓展阅读材料。
            要求：
            1. 提供该知识点的背景知识和发展历程
            2. 推荐相关的经典论文、书籍或在线资源
            3. 介绍该领域的前沿进展和实际应用场景
            4. 包含思考题，引导学生深入探索
            5. 输出格式为Markdown，内容丰富且有深度""")
    String generateReadingMaterial(
            @UserMessage String topic,
            @V("knowledge") String knowledge);
}
