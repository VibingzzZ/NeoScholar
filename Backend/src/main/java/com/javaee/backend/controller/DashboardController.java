package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.po.dto.DashboardStatsDTO;
import com.javaee.backend.po.dto.LearningAssessmentDTO;
import com.javaee.backend.service.DashboardService;
import com.javaee.backend.service.KnowledgeBaseService;
import com.javaee.backend.service.LearningAssessmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Dashboard 数据统计接口
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private LearningAssessmentService assessmentService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 获取用户学习统计数据
     */
    @GetMapping("/stats/{userId}")
    public Result<DashboardStatsDTO> getStats(@PathVariable Long userId) {
        log.info("查询 Dashboard 统计数据, userId: {}", userId);
        DashboardStatsDTO stats = dashboardService.getStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取用户多维度学习评估报告
     */
    @GetMapping("/assessment/{userId}")
    public Result<LearningAssessmentDTO> getAssessment(@PathVariable Long userId) {
        log.info("查询学习评估报告, userId: {}", userId);
        LearningAssessmentDTO assessment = assessmentService.assess(userId);
        return Result.success(assessment);
    }

    /**
     * 获取课程知识库目录
     */
    @GetMapping("/knowledge-base/index")
    public Result<Map<String, Object>> getKnowledgeBaseIndex() {
        log.info("查询知识库目录");
        Map<String, Object> index = knowledgeBaseService.getIndex();
        return Result.success(index);
    }
}
