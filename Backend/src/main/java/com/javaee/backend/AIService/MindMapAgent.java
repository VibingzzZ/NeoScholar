package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface MindMapAgent {
    @SystemMessage("""
            你是一个思维导图生成专家。请根据给定的知识点，生成结构清晰的思维导图。
            要求：
            1. 以Markdown层级标题格式输出（# 中心主题, ## 一级分支, ### 二级分支）
            2. 层级不少于3层，每个节点有简洁的关键词描述
            3. 逻辑关系清晰，覆盖知识点的核心概念、子概念及其关联
            4. 适合学生用于复习和知识梳理""")
    String generateMindMap(
            @UserMessage String topic,
            @V("knowledge") String knowledge);
}
