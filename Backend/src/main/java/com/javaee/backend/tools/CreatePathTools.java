package com.javaee.backend.tools;

import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.service.PathPlanningService;
import com.javaee.backend.service.StudentProfileService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 学习路径创建工具 — AI 在聊天中为用户规划学习路线后，
 * 可调用此工具将规划结果保存为正式的学习路径。
 */
@Slf4j
@Service("CreatePathTools")
public class CreatePathTools {

    @Autowired
    private StudentProfileService profileService;

    @Lazy
    @Autowired
    private PathPlanningService pathPlanningService;

    /**
     * 根据用户的活跃画像自动生成一条学习路径并保存到数据库。
     * AI 在完成学习规划讨论后调用此工具，无需用户手动操作。
     *
     * @param userId 用户 ID（从系统上下文获取，不要反问用户）
     * @return 创建结果描述
     */
    @Tool("根据用户的活跃画像自动生成并保存一条学习路径。当用户在对话中表达了学习意愿或AI已完成学习规划讨论后调用此工具。userId 从系统上下文中获取。")
    public String createLearningPath(Long userId) {
        if (userId == null) {
            log.warn("createLearningPath: userId 为空");
            return "创建失败：缺少 userId，请从系统上下文中的「当前用户ID」获取";
        }

        // 获取活跃画像
        StudentProfile profile;
        try {
            profile = profileService.getActiveByUserId(userId);
        } catch (Exception e) {
            log.error("获取活跃画像失败", e);
            return "创建学习路径失败：获取画像时出错，请先确认该用户已有画像。";
        }

        if (profile == null) {
            return "该用户还没有学习画像，无法生成学习路径。"
                    + "请引导用户告诉你 TA 的专业、学习目标和已有知识基础，"
                    + "你会自动调用 updateUserProfile 创建画像。";
        }

        if (isBlank(profile.getMajorOrField()) && isBlank(profile.getLearningGoal())) {
            return "该用户的画像信息不完整（缺少专业/学习目标），无法生成学习路径。"
                    + "请引导用户告诉你 TA 想学什么。";
        }

        // 调用现有的路径生成服务（AI 生成节点 + 保存到 DB）
        try {
            Long pathId = pathPlanningService.generateAndSavePath(profile);
            if (pathId != null) {
                log.info("AI 聊天中创建学习路径成功, userId: {}, pathId: {}", userId, pathId);
                return "学习路径已创建成功！路径ID: " + pathId
                        + "。用户可在「学习路径」页面查看详情。";
            } else {
                return "学习路径生成失败：AI 返回了空结果，请稍后重试。";
            }
        } catch (Exception e) {
            log.error("创建学习路径失败", e);
            return "创建学习路径失败：" + e.getMessage();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
