package com.javaee.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.QuizScoringAIService;
import com.javaee.backend.config.Result;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.entity.QuizAttempt;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.mapper.QuizAttemptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 测验评分与结果查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizScoringAIService scoringService;

    @Autowired
    private QuizAttemptMapper quizAttemptMapper;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 提交测验答案并获取 AI 评分
     */
    @PostMapping("/submit")
    public Result<QuizAttempt> submitQuiz(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        Long resourceId = ((Number) body.get("resourceId")).longValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> answers = (Map<String, Object>) body.get("answers");

        // 获取原始测验内容
        LearningResource resource = learningResourceMapper.selectById(resourceId);
        if (resource == null || !"quiz".equals(resource.getResourceType())) {
            return Result.error("测验资源不存在或类型不正确");
        }

        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            return Result.error("作答数据格式错误");
        }

        // AI 评分
        String scoringResult;
        try {
            scoringResult = scoringService.scoreAnswers(resource.getContentText(), answersJson);
        } catch (Exception e) {
            log.error("AI 评分失败, resourceId: {}", resourceId, e);
            return Result.error("AI 评分服务暂时不可用，请稍后重试");
        }

        // 清洗评分结果：只提取JSON对象（忽略AI可能输出的前缀/后缀文字和```包裹）
        String cleanJson = scoringResult.trim();
        // 去掉 ```json ... ``` 包裹
        cleanJson = cleanJson.replaceAll("```json\\s*", "").replaceAll("```", "");
        // 找到第一个 { 和最后一个 }，截取 JSON 部分
        int start = cleanJson.indexOf('{');
        int end = cleanJson.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleanJson = cleanJson.substring(start, end + 1);
        }
        log.info("清洗后评分JSON长度: {}", cleanJson.length());

        // 解析评分结果
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setResourceId(resourceId);
        attempt.setAnswersJson(answersJson);
        attempt.setScoresJson(scoringResult);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> scoreMap = objectMapper.readValue(cleanJson, Map.class);
            attempt.setTotalScore((Integer) scoreMap.getOrDefault("totalScore", 0));
            attempt.setEarnedScore((Integer) scoreMap.getOrDefault("earnedScore", 0));
            attempt.setFeedback((String) scoreMap.getOrDefault("feedback", ""));
        } catch (Exception e) {
            log.warn("解析评分结果失败, 原始结果: {}", scoringResult);
            attempt.setTotalScore(100);
            attempt.setEarnedScore(0);
            attempt.setFeedback(scoringResult);
        }

        attempt.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        quizAttemptMapper.insert(attempt);

        log.info("测验评分完成, attemptId: {}, score: {}/{}", attempt.getId(),
                attempt.getEarnedScore(), attempt.getTotalScore());

        return Result.success(attempt);
    }

    /**
     * 查询用户的测验历史
     */
    @GetMapping("/history/{userId}")
    public Result<List<QuizAttempt>> getHistory(@PathVariable Long userId) {
        LambdaQueryWrapper<QuizAttempt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizAttempt::getUserId, userId)
                .orderByDesc(QuizAttempt::getAttemptedAt);
        List<QuizAttempt> attempts = quizAttemptMapper.selectList(wrapper);
        return Result.success(attempts);
    }

    /**
     * 查询单次测验详情
     */
    @GetMapping("/detail/{attemptId}")
    public Result<QuizAttempt> getDetail(@PathVariable Long attemptId) {
        QuizAttempt attempt = quizAttemptMapper.selectById(attemptId);
        if (attempt == null) {
            return Result.error("测验记录不存在");
        }
        return Result.success(attempt);
    }
}
