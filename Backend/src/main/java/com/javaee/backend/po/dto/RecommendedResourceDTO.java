package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 推荐资源 DTO — 包含推荐理由和匹配分数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedResourceDTO {
    private Long id;
    private Long pathId;
    private Integer nodeIndex;
    private String title;
    private String resourceType;
    private String generatedByAgent;
    private String filePath;        // 下载路径
    private Integer relevanceScore;  // 匹配度评分 (0-100)
    private String reason;           // 推荐理由
    private Timestamp createdAt;
}
