package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard 统计数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {

    /** 学习路径总数 */
    private int pathCount;

    /** 已完成节点总数 */
    private int completedNodes;

    /** 活跃天数 */
    private int activeDays;

    /** AI 辅导总次数 */
    private int aiConsultCount;

    /** 每日活动记录（用于热力图） */
    private List<DailyActivity> dailyActivities;

    /** 活跃画像摘要 */
    private ProfileSummary activeProfile;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileSummary {
        private Long profileId;
        private String majorOrField;
        private String learningGoal;
        private String summary;
        private String source;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyActivity {
        /** 日期，格式 yyyy-MM-dd */
        private String date;
        /** 当日活动次数 */
        private int count;
    }
}
