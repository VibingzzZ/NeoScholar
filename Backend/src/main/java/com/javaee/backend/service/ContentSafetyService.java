package com.javaee.backend.service;

/**
 * 内容安全过滤服务 — 检查生成内容是否包含敏感/违规信息
 */
public interface ContentSafetyService {

    /**
     * 检查文本是否合规
     * @param text 待检查的文本
     * @return true = 安全可用，false = 包含违规内容需拦截
     */
    boolean isSafe(String text);

    /**
     * 获取拦截原因（仅在 isSafe 返回 false 时有效）
     */
    String getBlockReason();
}
