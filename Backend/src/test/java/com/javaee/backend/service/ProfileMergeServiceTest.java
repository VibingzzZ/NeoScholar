package com.javaee.backend.service;

import com.javaee.backend.AIService.ProfileMergeAIService;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import com.javaee.backend.po.dto.Profile;
import dev.langchain4j.internal.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.Timestamp;

@SpringBootTest
class ProfileMergeServiceTest {

    @Autowired
    private ProfileMergeAIService profileMergeAIService;

    @Autowired
    private ProfileMergeMapper profileMergeMapper;

    @Test
    void testLLMMerge() {
        Profile original = new Profile(
                "计算机科学",
                "我想掌握机器学习和深度学习的基础知识",
                "Python, Java, 基础数学",
                "逻辑思维",
                "{\"math\": \"代数错误\", \"coding\": \"语法错误\"}",
                "文字交流"
        );

        Profile newProfile = new Profile(
                "计算机科学",
                "我想掌握机器学习和深度学习",
                "Python, Java, 线性代数",
                "逻辑思维",
                "{\"math\": \"代数错误\", \"coding\": \"语法错误\"}",
                "文字交流"
        );

        String result = String.valueOf(profileMergeAIService.mergeProfiles(original, newProfile));

        System.out.println("LLM合并结果:");
        System.out.println(result);
    }

    @Test
    void testProfileMerge() {
        StudentProfile profile1 = new StudentProfile();
        profile1.setId(1L);
        profile1.setUserId(100L);
        profile1.setMajorOrField("计算机科学");
        profile1.setLearningGoal(createText("我想掌握机器学习和深度学习的基础知识"));
        profile1.setKnowledgeBase(createText("Python, Java, 基础数学"));
        profile1.setCognitiveStyle("逻辑思维");
        profile1.setCommonMistakes(Json.fromJson("{\"math\": \"代数错误\", \"coding\": \"语法错误\"}",Json.class));
        profile1.setInteractionPreference("文字交流");
        profile1.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        StudentProfile profile2 = new StudentProfile();
        profile2.setId(2L);
        profile2.setUserId(101L);
        profile2.setMajorOrField("人工智能");
        profile2.setLearningGoal(createText("深入学习神经网络和自然语言处理"));
        profile2.setKnowledgeBase(createText("Python, TensorFlow, PyTorch"));
        profile2.setCognitiveStyle("视觉学习");
        profile2.setCommonMistakes(Json.fromJson("{\"math\": \"微积分错误\", \"coding\": \"逻辑错误\"}", Json.class));
        profile2.setInteractionPreference("图表交流");
        profile2.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("测试数据准备完成");
        System.out.println("Profile 1: " + profile1);
        System.out.println("Profile 2: " + profile2);
    }

    private Text createText(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc.createTextNode(content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}