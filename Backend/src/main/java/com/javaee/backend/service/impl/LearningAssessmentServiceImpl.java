package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.entity.*;
import com.javaee.backend.mapper.*;
import com.javaee.backend.po.dto.LearningAssessmentDTO;
import com.javaee.backend.service.LearningAssessmentService;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LearningAssessmentServiceImpl implements LearningAssessmentService {

    @Autowired
    private QuizAttemptMapper quizAttemptMapper;

    @Autowired
    private LearningPathsMapper learningPathsMapper;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Autowired
    private ActivityLogMapper activityLogMapper;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public LearningAssessmentDTO assess(Long userId) {
        LearningAssessmentDTO result = new LearningAssessmentDTO();

        StudentProfile profile = studentProfileService.getActiveByUserId(userId);

        // 1. 学习行为数据
        LearningAssessmentDTO.LearningBehavior behavior = buildBehavior(userId);
        result.setBehavior(behavior);

        // 2. 各维度评分
        Map<String, Integer> dimScores = new LinkedHashMap<>();

        // 知识掌握度：基于测验平均分
        dimScores.put("知识掌握", behavior.getAvgQuizScore() != null
                ? behavior.getAvgQuizScore().intValue() : 50);

        // 学习投入度：基于学习天数
        int engagementScore = Math.min(100, behavior.getTotalStudyDays() * 5);
        dimScores.put("学习投入", Math.max(20, engagementScore));

        // 实践能力：基于测验次数
        int practiceScore = Math.min(100, behavior.getTotalQuizzes() * 15);
        dimScores.put("实践能力", Math.max(20, practiceScore));

        // 进步速度：基于学习路径完成情况
        int progressScore = calculateProgressScore(userId);
        dimScores.put("进步速度", progressScore);

        result.setDimensionScores(dimScores);

        // 3. 综合评分
        int overall = (int) dimScores.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(50);
        result.setOverallScore(overall);

        if (overall >= 85) result.setOverallLevel("优秀");
        else if (overall >= 70) result.setOverallLevel("良好");
        else if (overall >= 55) result.setOverallLevel("中等");
        else result.setOverallLevel("需努力");

        // 4. 知识点掌握情况（从测验结果中提取）
        List<QuizAttempt> attempts = getQuizAttempts(userId);
        Map<String, List<Integer>> topicScores = extractTopicScores(attempts);

        List<LearningAssessmentDTO.KnowledgePoint> mastered = new ArrayList<>();
        List<LearningAssessmentDTO.KnowledgePoint> weak = new ArrayList<>();

        for (var entry : topicScores.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
            var kp = new LearningAssessmentDTO.KnowledgePoint();
            kp.setName(entry.getKey());
            kp.setMasteryLevel((int) avg);
            if (avg >= 70) mastered.add(kp);
            else weak.add(kp);
        }

        result.setMasteredPoints(mastered);
        result.setWeakPoints(weak);

        // 5. 改进建议
        List<String> suggestions = generateSuggestions(profile, behavior, overall, dimScores, weak);
        result.setSuggestions(suggestions);

        log.info("学习评估完成, userId: {}, overall: {}/{}", userId, overall, result.getOverallLevel());

        return result;
    }

    private LearningAssessmentDTO.LearningBehavior buildBehavior(Long userId) {
        var behavior = new LearningAssessmentDTO.LearningBehavior();

        // 活跃天数
        LambdaQueryWrapper<ActivityLog> actWrapper = new LambdaQueryWrapper<>();
        actWrapper.eq(ActivityLog::getUserId, userId);
        List<ActivityLog> logs = activityLogMapper.selectList(actWrapper);
        behavior.setTotalStudyDays(logs.size());

        // 学习节奏分析
        if (logs.size() >= 30) {
            behavior.setStudyRhythm("规律");
        } else if (logs.size() >= 10) {
            behavior.setStudyRhythm("波动");
        } else {
            behavior.setStudyRhythm("突击");
        }

        // 资源数量
        LambdaQueryWrapper<LearningResource> resWrapper = new LambdaQueryWrapper<>();
        // 通过学习路径间接关联资源
        LambdaQueryWrapper<LearningPaths> pathWrapper = new LambdaQueryWrapper<>();
        pathWrapper.eq(LearningPaths::getUserId, userId);
        List<LearningPaths> paths = learningPathsMapper.selectList(pathWrapper);
        int totalResources = 0;
        for (LearningPaths p : paths) {
            LambdaQueryWrapper<LearningResource> rw = new LambdaQueryWrapper<>();
            rw.eq(LearningResource::getPathId, p.getId());
            totalResources += learningResourceMapper.selectCount(rw);
        }
        behavior.setTotalResources(totalResources);

        // 测验数据
        List<QuizAttempt> quizAttempts = getQuizAttempts(userId);
        behavior.setTotalQuizzes(quizAttempts.size());

        if (!quizAttempts.isEmpty()) {
            double avgScore = quizAttempts.stream()
                    .filter(q -> q.getTotalScore() != null && q.getTotalScore() > 0)
                    .mapToDouble(q -> q.getEarnedScore() * 100.0 / q.getTotalScore())
                    .average()
                    .orElse(0);
            behavior.setAvgQuizScore(Math.round(avgScore * 10.0) / 10.0);
        }

        // 参与度判断
        if (behavior.getTotalStudyDays() >= 20 && behavior.getTotalQuizzes() >= 5) {
            behavior.setEngagementLevel("高");
        } else if (behavior.getTotalStudyDays() >= 5) {
            behavior.setEngagementLevel("中");
        } else {
            behavior.setEngagementLevel("低");
        }

        return behavior;
    }

    private int calculateProgressScore(Long userId) {
        LambdaQueryWrapper<LearningPaths> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPaths::getUserId, userId);
        List<LearningPaths> paths = learningPathsMapper.selectList(wrapper);
        if (paths.isEmpty()) return 20;

        double avgProgress = paths.stream()
                .mapToDouble(p -> {
                    if (p.getNodesJson() == null) return 0;
                    try {
                        var nodes = objectMapper.readTree(p.getNodesJson());
                        int total = nodes.size();
                        if (total == 0) return 0;
                        int current = p.getCurrentNodeIndex() != null ? p.getCurrentNodeIndex() : 0;
                        return current * 100.0 / total;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .average()
                .orElse(0);
        return (int) Math.min(100, avgProgress + 20);
    }

    private List<QuizAttempt> getQuizAttempts(Long userId) {
        LambdaQueryWrapper<QuizAttempt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizAttempt::getUserId, userId)
                .orderByDesc(QuizAttempt::getAttemptedAt);
        return quizAttemptMapper.selectList(wrapper);
    }

    private Map<String, List<Integer>> extractTopicScores(List<QuizAttempt> attempts) {
        Map<String, List<Integer>> map = new LinkedHashMap<>();
        for (QuizAttempt a : attempts) {
            if (a.getScoresJson() == null) continue;
            try {
                var scoreMap = objectMapper.readTree(a.getScoresJson());
                if (scoreMap.has("masteredTopics")) {
                    for (var topic : scoreMap.get("masteredTopics")) {
                        map.computeIfAbsent(topic.asText(), k -> new ArrayList<>()).add(80);
                    }
                }
                if (scoreMap.has("weakTopics")) {
                    for (var topic : scoreMap.get("weakTopics")) {
                        map.computeIfAbsent(topic.asText(), k -> new ArrayList<>()).add(40);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private List<String> generateSuggestions(StudentProfile profile,
                                              LearningAssessmentDTO.LearningBehavior behavior,
                                              int overall,
                                              Map<String, Integer> dimScores,
                                              List<LearningAssessmentDTO.KnowledgePoint> weakPoints) {
        List<String> suggestions = new ArrayList<>();

        if (overall < 55) {
            suggestions.add("建议从基础概念开始，利用思维导图资源梳理知识框架");
        }

        // 基于画像的个性化建议
        if (profile != null) {
            if ("实践操作型".equals(profile.getCognitiveStyle())) {
                suggestions.add("作为实践型学习者，建议优先完成实操案例和编程练习，在动手中学");
            } else if ("理论抽象型".equals(profile.getCognitiveStyle())) {
                suggestions.add("建议先通过PPT大纲和拓展阅读理解理论知识，再逐步过渡到实践");
            }
        }

        // 基于弱项的建议
        if (!weakPoints.isEmpty()) {
            String topics = weakPoints.stream()
                    .map(LearningAssessmentDTO.KnowledgePoint::getName)
                    .limit(3)
                    .collect(Collectors.joining("、"));
            suggestions.add("薄弱知识点「" + topics + "」需要加强复习，建议针对性练习");
        }

        // 基于投入度的建议
        if ("低".equals(behavior.getEngagementLevel())) {
            suggestions.add("学习投入度偏低，建议每天安排固定学习时间，保持学习连续性");
        }

        // 通用建议
        if (behavior.getTotalQuizzes() < 3) {
            suggestions.add("建议多参与测验练习来检验学习效果，持续跟踪进步");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("继续保持当前的学习节奏，你做得很好！");
        }

        return suggestions;
    }
}
