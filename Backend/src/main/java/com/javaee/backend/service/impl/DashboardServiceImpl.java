package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.ChatMessage;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.mapper.ChatMessageMapper;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.po.dto.DashboardStatsDTO;
import com.javaee.backend.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        // 活跃天数 + 每日活动记录（从 chat_messages 按日期分组统计）
        LambdaQueryWrapper<ChatMessage> allMsgWrapper = new LambdaQueryWrapper<>();
        allMsgWrapper.eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getRole, "user")
                .select(ChatMessage::getCreated_at);
        List<ChatMessage> allUserMessages = chatMessageMapper.selectList(allMsgWrapper);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dateCountMap = allUserMessages.stream()
                .filter(m -> m.getCreated_at() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getCreated_at().toLocalDateTime().toLocalDate().format(fmt),
                        Collectors.counting()
                ));

        stats.setActiveDays(dateCountMap.size());

        List<DashboardStatsDTO.DailyActivity> dailyActivities = dateCountMap.entrySet().stream()
                .map(e -> new DashboardStatsDTO.DailyActivity(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparing(DashboardStatsDTO.DailyActivity::getDate))
                .collect(Collectors.toList());
        stats.setDailyActivities(dailyActivities);

        log.info("Dashboard统计: userId={}, 路径数={}, 已完成节点={}, 活跃天数={}, AI辅导次数={}",
                userId, stats.getPathCount(), stats.getCompletedNodes(),
                stats.getActiveDays(), stats.getAiConsultCount());

        return stats;
    }
}
