package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.po.dto.DashboardStatsDTO;
import com.javaee.backend.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats/{userId}")
    public Result<DashboardStatsDTO> getStats(@PathVariable Long userId) {
        log.info("查询 Dashboard 统计数据, userId: {}", userId);
        return Result.success(dashboardService.getStats(userId));
    }
}
