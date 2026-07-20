package com.javaee.backend.service;

import com.javaee.backend.po.dto.RecommendedResourceDTO;

import java.util.List;

/**
 * 基于学生画像的学习资源推荐服务
 */
public interface ResourceRecommendationService {

    /**
     * 根据用户画像，对学习路径下的资源进行个性化排序推荐
     * @param userId 用户ID
     * @param pathId 学习路径ID（可选，为null时推荐所有资源）
     * @param limit  推荐数量上限
     * @return 排序后的推荐资源列表
     */
    List<RecommendedResourceDTO> recommendResources(Long userId, Long pathId, int limit);
}
