package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.ActivityLog;
import com.javaee.backend.entity.ChatMessage;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.mapper.ActivityLogMapper;
import com.javaee.backend.mapper.ChatMessageMapper;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.po.dto.DashboardStatsDTO;
import com.javaee.backend.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private LearningPathsMapper learningPathsMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ActivityLogMapper activityLogMapper;

    @Override
    public DashboardStatsDTO getStats(Long userId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 学习路径数
        LambdaQueryWrapper<LearningPaths> pathWrapper = new LambdaQueryWrapper<>();
        pathWrapper.eq(LearningPaths::getUserId, userId);
        List<LearningPaths> paths = learningPathsMapper.selectList(pathWrapper);
        stats.setPathCount(paths.size());

        // 已完成节点数（所有路径的 current_node_index 之和）
        int completedNodes = paths.stream()
                .mapToInt(p -> p.getCurrentNodeIndex() != null ? p.getCurrentNodeIndex() : 0)
                .sum();
        stats.setCompletedNodes(completedNodes);

        // AI 辅导次数（chat_messages 中 role='user' 的记录数）
        LambdaQueryWrapper<ChatMessage> chatWrapper = new LambdaQueryWrapper<>();
        chatWrapper.eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getRole, "user");
        long aiConsultCount = chatMessageMapper.selectCount(chatWrapper);
        stats.setAiConsultCount((int) aiConsultCount);

        // ==================== 每日活动数据（合并 activity_log + chat_messages） ====================
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Integer> dateCountMap = new TreeMap<>(); // TreeMap 自动按日期排序

        // 来源1: activity_log 表 —— 所有业务功能的计数汇总（AI咨询、测验生成、学习进度等）
        LambdaQueryWrapper<ActivityLog> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.eq(ActivityLog::getUserId, userId);
        List<ActivityLog> activityLogs = activityLogMapper.selectList(activityWrapper);
        for (ActivityLog log : activityLogs) {
            if (log.getActivityDate() != null) {
                String key = log.getActivityDate().format(fmt);
                dateCountMap.merge(key, log.getCount() != null ? log.getCount() : 0, Integer::sum);
            }
        }

        // 来源2: 如果 activity_log 尚无数据（兼容历史数据），回退到 chat_messages 统计
        if (dateCountMap.isEmpty()) {
            LambdaQueryWrapper<ChatMessage> allMsgWrapper = new LambdaQueryWrapper<>();
            allMsgWrapper.eq(ChatMessage::getUserId, userId)
                    .eq(ChatMessage::getRole, "user")
                    .select(ChatMessage::getCreated_at);
            List<ChatMessage> allUserMessages = chatMessageMapper.selectList(allMsgWrapper);
            for (ChatMessage m : allUserMessages) {
                if (m.getCreated_at() != null) {
                    String key = m.getCreated_at().toLocalDateTime().toLocalDate().format(fmt);
                    dateCountMap.merge(key, 1, Integer::sum);
                }
            }
        }

        stats.setActiveDays(dateCountMap.size());

        List<DashboardStatsDTO.DailyActivity> dailyActivities = dateCountMap.entrySet().stream()
                .map(e -> new DashboardStatsDTO.DailyActivity(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        stats.setDailyActivities(dailyActivities);

        log.info("Dashboard统计: userId={}, 路径数={}, 已完成节点={}, 活跃天数={}, AI辅导次数={}",
                userId, stats.getPathCount(), stats.getCompletedNodes(),
                stats.getActiveDays(), stats.getAiConsultCount());

        return stats;
    }
}
