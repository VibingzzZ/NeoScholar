package com.javaee.backend.po.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPathNodeDTO {
    private Integer stepOrder;
    private String title;
    private String description;
    private Integer estimatedDays;
}