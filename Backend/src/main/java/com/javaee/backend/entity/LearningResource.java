package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("learning_resources")
public class LearningResource {
    private Long id;
    private Long pathId;
    private Integer nodeIndex;
    private String title;
    private String  contentText;
    private String filePath;
    private String resourceType;
    private String generatedByAgent;
    private Timestamp createdAt;
}