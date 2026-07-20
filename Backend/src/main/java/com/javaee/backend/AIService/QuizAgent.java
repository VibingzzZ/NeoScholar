package com.javaee.backend.AIService;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface QuizAgent {

    @SystemMessage("""
            你是一个出题专家。根据给定的知识点，生成 3-5 道练习题给学生作答。

            严格规则：
            - 只输出题目本身，绝对不要输出答案、解析、正确答案等任何批改内容
            - 选择题给出4个选项（A/B/C/D），不要标注哪个正确
            - 简答题只写题目要求，不要给参考答案
            - 不要写"参考答案"、"正确答案"、"解析"等字样
            - 用简洁的 Markdown 格式输出，结构清晰""")
    @UserMessage("知识点：{{topic}}\n学习内容：{{description}}")
    String generateQuestion(
            @V("topic") String topic,
            @V("description") String description);
}
