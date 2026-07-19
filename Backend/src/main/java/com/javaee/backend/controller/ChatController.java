package com.javaee.backend.controller;

import com.javaee.backend.AIService.ConsultantService;

import com.javaee.backend.config.Result;
import com.javaee.backend.entity.ChatMessage;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.po.dto.ChatRequest;
import com.javaee.backend.service.*;
import com.javaee.backend.tools.ProfileExtractionTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("api/chat")
public class ChatController {

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private ProfileExtractionService profileExtractionService;

    @Autowired
    private ProfileExtractionTools profileExtractionTools;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest chatRequest) {
        String chatId = chatRequest.getChatId();
        Long userId = chatRequest.getUserId();

        // 注入活跃画像上下文到用户问题中
        String enrichedQuestion = enrichWithProfileContext(
                chatRequest.getQuestion(), userId);

        chatMessageService.save(chatRequest.getQuestion(), chatId, userId, "user");

        StringBuilder fullReply = new StringBuilder();
        return consultantService.chatWithStream(
                        enrichedQuestion,
                        chatId
                )
                .doOnNext(fullReply::append)
                .concatWith(Mono.just("[DONE]"))
                .doOnComplete(() -> {
                    String assistantReply = fullReply.toString();
                    chatMessageService.save(assistantReply, chatId, userId, "assistant");
                    // 异步提取画像：每轮对话后自动分析用户发言
                    triggerProfileExtraction(chatRequest.getQuestion(), userId);
                });
    }

    /**
     * 将用户 ID 和活跃画像上下文注入到问题中。
     * userId 始终注入，确保 AI 调用 Tool 时不需要问用户。
     * 画像信息作为隐藏上下文附加在问题末尾。
     * 如果 DB 尚未完成迁移（新列不存在），静默降级跳过。
     */
    private String enrichWithProfileContext(String question, Long userId) {
        // 始终注入 userId，AI 调用 updateUserProfile / generateLearningResource 等 Tool 时必需
        String ctxPrefix = "\n\n[系统上下文 - 你不需要询问以下信息，直接使用：]\n"
                + "- 当前用户ID: " + userId + "\n";

        try {
            StudentProfile profile = studentProfileService.getActiveByUserId(userId);
            if (profile != null) {
                ctxPrefix += buildProfileContextLines(profile);
            }
        } catch (Exception e) {
            // DB 迁移尚未完成时静默降级
        }

        return ctxPrefix + "\n---\n\n" + question;
    }

    /**
     * 异步提取画像：用独立 AI 调用分析用户发言中的画像信息，
     * 不依赖流式对话中的 Tool 触发，更可靠。
     */
    private void triggerProfileExtraction(String userMessage, Long userId) {
        CompletableFuture.runAsync(() -> {
            try {
                ProfileExtractionResult result = profileExtractionService.extract(userMessage);
                if (result == null) return;

                profileExtractionTools.updateUserProfile(
                        userId,
                        result.getMajorOrField(),
                        result.getLearningGoal(),
                        result.getKnowledgeBase(),
                        result.getCognitiveStyle(),
                        result.getCommonMistakes(),
                        result.getInteractionPreference()
                );
            } catch (Exception e) {
                // 画像提取失败不影响主流程
            }
        });
    }

    /**
     * 查询用户的聊天历史
     */
    @GetMapping("/history/{userId}")
    public Result<List<ChatMessage>> getHistory(@PathVariable Long userId) {
        List<ChatMessage> history = chatMessageService.getHistoryByUserId(userId);
        return Result.success(history);
    }

    private String buildProfileContextLines(StudentProfile profile) {
        StringBuilder ctx = new StringBuilder();
        if (profile.getMajorOrField() != null && !profile.getMajorOrField().isBlank()) {
            ctx.append("- 专业/领域: ").append(profile.getMajorOrField()).append("\n");
        }
        if (profile.getLearningGoal() != null && !profile.getLearningGoal().isBlank()) {
            ctx.append("- 学习目标: ").append(profile.getLearningGoal()).append("\n");
        }
        if (profile.getKnowledgeBase() != null && !profile.getKnowledgeBase().isBlank()) {
            ctx.append("- 已有知识基础: ").append(profile.getKnowledgeBase()).append("\n");
        }
        if (profile.getCognitiveStyle() != null && !profile.getCognitiveStyle().isBlank()) {
            ctx.append("- 思维/学习风格: ").append(profile.getCognitiveStyle()).append("\n");
        }
        if (profile.getCommonMistakes() != null && !profile.getCommonMistakes().isBlank()) {
            ctx.append("- 常见错误: ").append(profile.getCommonMistakes()).append("\n");
        }
        if (profile.getInteractionPreference() != null && !profile.getInteractionPreference().isBlank()) {
            ctx.append("- 交互偏好: ").append(profile.getInteractionPreference()).append("\n");
        }
        return ctx.toString();
    }
}