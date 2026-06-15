package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("learning_paths")
public class LearningPaths  {
    private Long id;
    private Long userId;
    private String pathName;
    private String nodesJson;
    private Integer currentNodeIndex;
    private Integer status;
    private Timestamp updatedAt;

}