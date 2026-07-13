package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private int pathCount;
    private int completedNodes;
    private int activeDays;
    private int aiConsultCount;
    private List<DailyActivity> dailyActivities;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyActivity {
        private String date;
        private int count;
    }
}
