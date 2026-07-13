package com.javaee.backend.service;

import com.javaee.backend.entity.LearningPaths;

import java.util.List;

/**
 * 学习路径查询与进度管理服务
 */
public interface LearningPathService {

    /**
     * 查询用户的所有学习路径
     */
    List<LearningPaths> listByUserId(Long userId);

    /**
     * 根据 ID 查询路径详情
     */
    LearningPaths getById(Long pathId);

    /**
     * 更新学习进度
     */
    void updateProgress(Long pathId, Integer nodeIndex);

    /**
     * 根据用户 ID 生成学习路径（自动获取该用户最新画像）
     */
    Long generatePath(Long userId);
}
