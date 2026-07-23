package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 多维度学习评估结果 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningAssessmentDTO {

    // 综合评分 (0-100)
    private Integer overallScore;
    private String overallLevel;    // 优秀/良好/中等/需努力

    // 各维度评分 (0-100)
    private Map<String, Integer> dimensionScores;  // e.g. 知识掌握, 实践能力, 学习投入, 进步速度

    // 知识点掌握情况
    private List<KnowledgePoint> masteredPoints;    // 已掌握知识点
    private List<KnowledgePoint> weakPoints;         // 薄弱知识点

    // 学习行为分析
    private LearningBehavior behavior;

    // 改进建议
    private List<String> suggestions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KnowledgePoint {
        private String name;
        private Integer masteryLevel;  // 0-100
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearningBehavior {
        private Integer totalStudyDays;
        private Integer totalResources;
        private Integer totalQuizzes;
        private Double avgQuizScore;
        private String studyRhythm;     // 规律/波动/突击
        private String engagementLevel; // 高/中/低
    }
}
