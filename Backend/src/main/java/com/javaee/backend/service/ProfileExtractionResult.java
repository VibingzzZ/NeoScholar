package com.javaee.backend.service;

import lombok.Data;

/**
 * AI 画像提取的返回结果 — 与前端 StudentProfile 字段对应，
 * null 表示该字段未从对话中提取到。
 */
@Data
public class ProfileExtractionResult {
    private String majorOrField;
    private String learningGoal;
    private String knowledgeBase;
    private String cognitiveStyle;
    private String commonMistakes;
    private String interactionPreference;
}
