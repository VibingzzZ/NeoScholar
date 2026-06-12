package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;


@AiService
public interface PathPlanningAIService {

    @SystemMessage(fromResource = "PathPlanningAIPrompt.txt")
    @UserMessage("请根据学生画像为其生成学习路径。专业：{{major}}，学习目标：{{goal}}，现有知识：{{knowledge}}")

    String evaluatePath(
            @V("major") String major,
            @V("goal") String goal,
            @V("knowledge") String knowledge
    );
}
