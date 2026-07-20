package com.javaee.backend.service;

import com.javaee.backend.po.dto.LearningAssessmentDTO;

/**
 * 多维度学习效果评估服务
 */
public interface LearningAssessmentService {

    /**
     * 生成用户的综合学习评估报告
     * @param userId 用户ID
     * @return 多维度评估结果
     */
    LearningAssessmentDTO assess(Long userId);
}
