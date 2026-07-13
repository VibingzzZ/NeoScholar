package com.javaee.backend.service;

import com.javaee.backend.po.dto.DashboardStatsDTO;

/**
 * Dashboard 数据统计服务
 */
public interface DashboardService {

    /**
     * 获取用户的学习统计数据
     */
    DashboardStatsDTO getStats(Long userId);
}
