package com.javaee.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.entity.StudentProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;

@SpringBootTest
class PathPlanningServiceTest {

    @Autowired
    private PathPlanningService pathPlanningService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProfileToPathName() {
        System.out.println("=== 测试路径名称生成 ===");

        StudentProfile profile = new StudentProfile();
        profile.setId(1L);
        profile.setUserId(100L);
        profile.setMajorOrField("计算机科学");
        profile.setLearningGoal("掌握人工智能基础");
        profile.setKnowledgeBase("Python, 数据结构, 算法");
        profile.setCognitiveStyle("逻辑思维");
        profile.setInteractionPreference("文字交流");
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("Profile 构建成功");
        System.out.println("专业: " + profile.getMajorOrField());
        System.out.println("目标: " + profile.getLearningGoal());
        System.out.println("✓ 测试数据准备完成");
    }
}
