package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("activity_log")
// 热力图记录
public class ActivityLog {
    private Long id;
    private Long userId;
    private LocalDate activityDate;
    private Integer count;
}
