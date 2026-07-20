package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.po.dto.RecommendedResourceDTO;
import com.javaee.backend.service.ResourceRecommendationService;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于画像匹配的资源推荐实现。
 * 策略：根据学生画像的6个维度与资源类型的对应关系计算匹配度分数。
 */
@Slf4j
@Service
public class ResourceRecommendationServiceImpl implements ResourceRecommendationService {

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    // 认知风格 → 优先资源类型映射
    private static final Map<String, List<String>> STYLE_TYPE_MAP = Map.of(
            "逻辑分析型", List.of("mindmap", "quiz", "codecase"),
            "直觉创新型", List.of("reading", "mindmap", "ppt"),
            "实践操作型", List.of("codecase", "quiz", "ppt"),
            "理论抽象型", List.of("ppt", "reading", "mindmap")
    );

    // 交互偏好 → 优先资源类型映射
    private static final Map<String, List<String>> PREFERENCE_TYPE_MAP = Map.of(
            "详细讲解", List.of("ppt", "reading"),
            "简洁提示", List.of("mindmap", "quiz"),
            "引导式提问", List.of("quiz", "codecase"),
            "案例驱动", List.of("codecase", "reading")
    );

    @Override
    public List<RecommendedResourceDTO> recommendResources(Long userId, Long pathId, int limit) {
        StudentProfile profile = studentProfileService.getActiveByUserId(userId);

        // 获取资源列表
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        if (pathId != null) {
            wrapper.eq(LearningResource::getPathId, pathId);
        }
        wrapper.orderByDesc(LearningResource::getCreatedAt);
        List<LearningResource> resources = learningResourceMapper.selectList(wrapper);

        if (resources.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算每项资源的推荐分数
        List<RecommendedResourceDTO> scored = resources.stream()
                .map(r -> {
                    RecommendedResourceDTO dto = new RecommendedResourceDTO();
                    dto.setId(r.getId());
                    dto.setPathId(r.getPathId());
                    dto.setNodeIndex(r.getNodeIndex());
                    dto.setTitle(r.getTitle());
                    dto.setResourceType(r.getResourceType());
                    dto.setGeneratedByAgent(r.getGeneratedByAgent());
                    dto.setFilePath(r.getFilePath());
                    dto.setCreatedAt(r.getCreatedAt());

                    int score = calculateScore(r, profile);
                    dto.setRelevanceScore(score);
                    dto.setReason(generateReason(score, r, profile));
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());

        log.info("为用户 {} 推荐了 {} 条资源（共 {} 条候选）", userId,
                Math.min(limit, scored.size()), scored.size());

        return scored.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 计算资源与画像的匹配度分数
     */
    private int calculateScore(LearningResource resource, StudentProfile profile) {
        int score = 50; // 基础分

        if (profile == null) return score;

        String type = resource.getResourceType();
        if (type == null) return score;

        // 认知风格匹配 (+0~30)
        String style = profile.getCognitiveStyle();
        if (style != null && STYLE_TYPE_MAP.containsKey(style)) {
            List<String> preferred = STYLE_TYPE_MAP.get(style);
            int idx = preferred.indexOf(type);
            if (idx >= 0) {
                score += (30 - idx * 10); // 第一优先 +30, 第二 +20, 第三 +10
            }
        }

        // 交互偏好匹配 (+0~20)
        String preference = profile.getInteractionPreference();
        if (preference != null && PREFERENCE_TYPE_MAP.containsKey(preference)) {
            List<String> preferred = PREFERENCE_TYPE_MAP.get(preference);
            int idx = preferred.indexOf(type);
            if (idx >= 0) {
                score += (20 - idx * 10); // 第一优先 +20, 第二 +10
            }
        }

        // 知识基础相关性：资源类型为 quiz/codecase 对有知识基础的学生加分
        if (profile.getKnowledgeBase() != null && !profile.getKnowledgeBase().isBlank()) {
            if ("quiz".equals(type) || "codecase".equals(type)) {
                score += 5;
            }
        }

        return Math.min(100, score);
    }

    private String generateReason(int score, LearningResource r, StudentProfile profile) {
        if (profile == null) return "通用推荐";
        if (score >= 80) return "高度匹配你的学习风格和偏好";
        if (score >= 65) return "与你的学习偏好较为匹配";
        if (score >= 50) return "基础学习资源推荐";
        return "拓展学习参考";
    }
}
