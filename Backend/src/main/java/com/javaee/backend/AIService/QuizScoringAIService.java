package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface QuizScoringAIService {

    @SystemMessage("""
            你是测验评分专家。批改学生作答，直接返回以下JSON（只返回JSON，不要任何其他文字、解释或Markdown标记）：

            评分规则：
            - 总分100分，按题目数量均分（如5题则每题20分，3题则每题33分，有选择题和简答题按题量平分）
            - 选择题答对给满分，答错或未答给0分
            - 简答题根据要点完成度给部分分数
            - 每题必须给出correctAnswer（正确答案）和analysis（解析）
            - feedback写2-3句整体评价，语气积极鼓励
            - suggestions给出3条具体可行的学习建议

            {"totalScore":100,"earnedScore":60,"itemScores":[{"itemIndex":1,"maxScore":25,"earned":25,"correctAnswer":"C","analysis":"...","comment":"..."}],"feedback":"...","masteredTopics":[],"weakTopics":[],"suggestions":[]}""")
    @UserMessage("""
            批改测验。只返回JSON。

            题目：
            {{quizContent}}

            作答：
            {{answers}}""")
    String scoreAnswers(
            @V("quizContent") String quizContent,
            @V("answers") String studentAnswers
    );
}
