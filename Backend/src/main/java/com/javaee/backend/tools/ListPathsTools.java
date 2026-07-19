package com.javaee.backend.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.mapper.LearningPathsMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习路径查询工具 — AI 可以调用此工具查看用户已有的学习路径，
 * 避免反复询问用户「路径ID是什么」。
 */
@Slf4j
@Service("ListPathsTools")
public class ListPathsTools {

    @Autowired
    private LearningPathsMapper learningPathsMapper;

    /**
     * 查询指定用户的所有学习路径，返回简要列表供 AI 参考。
     *
     * @param userId 用户 ID（从系统上下文中获取，不要反问用户）
     * @return 路径列表摘要
     */
    @Tool("查询用户已有的学习路径列表。当用户提到'学习路径'、'生成资源'但未明确指定 pathId 时，先调用此工具查看用户有哪些路径。userId 从系统上下文中获取。")
    public String listUserPaths(Long userId) {
        if (userId == null) {
            return "错误：未提供 userId，请从系统上下文中的「当前用户ID」获取";
        }

        LambdaQueryWrapper<LearningPaths> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPaths::getUserId, userId)
                .orderByDesc(LearningPaths::getUpdatedAt);
        List<LearningPaths> paths = learningPathsMapper.selectList(wrapper);

        if (paths.isEmpty()) {
            return "该用户还没有创建任何学习路径。请引导用户先在「学习路径」页面生成一条路径，"
                    + "或者让用户告诉你 TA 想学什么，由你来帮 TA 规划学习内容。";
        }

        return paths.stream()
                .map(p -> String.format("- 路径ID: %d | 名称: %s | 进度: %d/%d",
                        p.getId(),
                        p.getPathName(),
                        p.getCurrentNodeIndex() != null ? p.getCurrentNodeIndex() : 0,
                        estimateNodeCount(p.getNodesJson())))
                .collect(Collectors.joining("\n"));
    }

    private int estimateNodeCount(String nodesJson) {
        if (nodesJson == null || nodesJson.isBlank()) return 0;
        // 简单计数 "stepOrder" 出现次数来估算节点数
        int count = 0;
        int idx = 0;
        while ((idx = nodesJson.indexOf("stepOrder", idx)) != -1) {
            count++;
            idx += 9;
        }
        return count;
    }
}
