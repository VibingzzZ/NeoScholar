package com.javaee.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 测验/练习作答记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("quiz_attempts")
public class QuizAttempt {
    private Long id;
    private Long userId;
    private Long resourceId;        // 关联的测验资源ID
    private String answersJson;     // 学生作答内容(JSON)
    private String scoresJson;      // AI评分结果(JSON)
    private Integer totalScore;     // 总分
    private Integer earnedScore;    // 得分
    private String feedback;        // AI综合反馈
    private Timestamp attemptedAt;
}
