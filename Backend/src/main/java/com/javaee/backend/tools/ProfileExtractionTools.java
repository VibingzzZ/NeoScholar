package com.javaee.backend.tools;


import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.service.StudentProfileService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户画像提取工具 — 从对话中自动识别并保存用户画像信息
 * <p>
 * AI 在聊天过程中发现用户透露了专业、学习目标、知识基础等信息时，
 * 调用此 Tool 自动更新画像，无需用户手动填写。
 */
@Slf4j
@Service("ProfileExtractionTools")
public class ProfileExtractionTools {

    @Autowired
    private StudentProfileService profileService;

    /**
     * 从对话中提取用户画像信息并保存。
     * 采用"智能合并"策略：非 null 且非空白的字段会覆盖已有数据，null 字段保持原值不变。
     *
     * @param userId                用户 ID（必填）
     * @param majorOrField          专业或学习领域，如"计算机科学大二"、"软件工程"
     * @param learningGoal          学习目标，如"掌握 Spring Boot"、"通过期末考试"
     * @param knowledgeBase         已有知识基础，如"会 Java 基础"、"了解数据库"
     * @param cognitiveStyle        思维/学习风格，如"先实践后理论"、"喜欢视觉化学习"
     * @param commonMistakes        常见错误模式，如"混淆抽象类和接口"
     * @param interactionPreference 交互偏好，如"喜欢代码示例"、"需要大量练习题"
     * @return 操作结果描述
     */
    @Tool("从对话中提取用户画像信息并更新到数据库。当用户在对话中透露了专业方向、学习目标、知识基础、思维风格、常见错误、交互偏好等信息时调用此工具。只传递你确定的信息，不确定的字段请传 null，null 字段不会覆盖已有数据。userId 为必填字段。")
    public String updateUserProfile(
            Long userId,
            String majorOrField,
            String learningGoal,
            String knowledgeBase,
            String cognitiveStyle,
            String commonMistakes,
            String interactionPreference
    ) {
        log.info("画像提取工具被调用, userId: {}", userId);

        if (userId == null) {
            log.warn("userId 为空，跳过画像更新");
            return "画像更新失败：缺少 userId";
        }

        // 1. 查询用户最新画像
        StudentProfile existing = profileService.getLatestByUserId(userId);
        boolean isNew = (existing == null);

        if (isNew) {
            existing = new StudentProfile();
            existing.setUserId(userId);
            log.info("用户 {} 尚无画像，将创建新画像", userId);
        } else {
            log.info("用户 {} 已有画像 ID: {}，将进行智能合并", userId, existing.getId());
        }

        // 2. 智能合并：只覆盖非 null 且非空白的字段
        int updatedFields = 0;

        if (isNotBlank(majorOrField)) {
            existing.setMajorOrField(majorOrField.trim());
            updatedFields++;
        }
        if (isNotBlank(learningGoal)) {
            existing.setLearningGoal(learningGoal.trim());
            updatedFields++;
        }
        if (isNotBlank(knowledgeBase)) {
            existing.setKnowledgeBase(knowledgeBase.trim());
            updatedFields++;
        }
        if (isNotBlank(cognitiveStyle)) {
            existing.setCognitiveStyle(cognitiveStyle.trim());
            updatedFields++;
        }
        if (isNotBlank(commonMistakes)) {
            existing.setCommonMistakes(commonMistakes.trim());
            updatedFields++;
        }
        if (isNotBlank(interactionPreference)) {
            existing.setInteractionPreference(interactionPreference.trim());
            updatedFields++;
        }

        if (updatedFields == 0) {
            log.info("没有需要更新的字段");
            return "画像无变化：所有传入字段均为空";
        }

        // 3. 保存
        if (isNew) {
            profileService.create(existing);
            log.info("创建用户画像成功, ID: {}, 更新字段数: {}", existing.getId(), updatedFields);
        } else {
            profileService.update(existing);
            log.info("更新用户画像成功, ID: {}, 更新字段数: {}", existing.getId(), updatedFields);
        }

        return String.format("用户画像已%s，更新了 %d 个字段", isNew ? "创建" : "更新", updatedFields);
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }
}
