package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("profile_merge_history")
public class ProfileMergeHistory {
    private Long id;
    private Long userId;
    private Long originalId;
    private Long targetId;
    private String resultSummary;
    private String status;
    private Timestamp mergedAt;
}
