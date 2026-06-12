package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPathNodeDTO {
    private Integer stepOrder;          // 节点顺序
    private String title;               // 节点标题
    private String description;         // 节点描述
    private Integer estimatedDays;      // 预计天数
    private String resourceType;        // "ppt" | "video" | "quiz" | "summary"
}