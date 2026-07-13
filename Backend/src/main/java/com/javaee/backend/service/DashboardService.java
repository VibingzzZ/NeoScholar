package com.javaee.backend.service;

import com.javaee.backend.po.dto.DashboardStatsDTO;

public interface DashboardService {
    DashboardStatsDTO getStats(Long userId);
}
