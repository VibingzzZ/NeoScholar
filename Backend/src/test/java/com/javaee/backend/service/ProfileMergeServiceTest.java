package com.javaee.backend.service;

import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;

@SpringBootTest
class ProfileMergeServiceTest {

    @Autowired
    private ProfileMergeMapper profileMergeMapper;

    @Test
    void testProfileMerge() {
        StudentProfile profile1 = new StudentProfile();
        profile1.setId(1L);
        profile1.setUserId(100L);
        profile1.setMajorOrField("计算机科学");
        profile1.setLearningGoal("我想掌握机器学习和深度学习的基础知识");
        profile1.setKnowledgeBase("Python, Java, 基础数学");
        profile1.setCognitiveStyle("逻辑思维");
        profile1.setCommonMistakes("{\"math\": \"代数错误\", \"coding\": \"语法错误\"}");
        profile1.setInteractionPreference("文字交流");
        profile1.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        StudentProfile profile2 = new StudentProfile();
        profile2.setId(2L);
        profile2.setUserId(101L);
        profile2.setMajorOrField("人工智能");
        profile2.setLearningGoal("深入学习神经网络和自然语言处理");
        profile2.setKnowledgeBase("Python, TensorFlow, PyTorch");
        profile2.setCognitiveStyle("视觉学习");
        profile2.setCommonMistakes("{\"math\": \"微积分错误\", \"coding\": \"逻辑错误\"}");
        profile2.setInteractionPreference("图表交流");
        profile2.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("测试数据准备完成");
        System.out.println("Profile 1: " + profile1);
        System.out.println("Profile 2: " + profile2);
    }
}
