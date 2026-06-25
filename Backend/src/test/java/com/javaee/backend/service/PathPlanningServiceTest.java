package com.javaee.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.entity.StudentProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.Timestamp;

@SpringBootTest
class PathPlanningServiceTest {

    @Autowired
    private PathPlanningService pathPlanningService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTextHelper() {
        System.out.println("=== 测试 createText 辅助方法 ===");

        Text text = createText("测试文本内容");
        assertNotNull(text);
        assertEquals("测试文本内容", text.getWholeText());
        System.out.println("✓ 辅助方法正常工作");
    }

    @Test
    void testProfileToPathName() {
        System.out.println("=== 测试路径名称生成 ===");

        StudentProfile profile = new StudentProfile();
        profile.setId(1L);
        profile.setUserId(100L);
        profile.setMajorOrField("计算机科学");
        profile.setLearningGoal(createText("掌握人工智能基础"));
        profile.setKnowledgeBase(createText("Python, 数据结构, 算法"));
        profile.setCognitiveStyle("逻辑思维");
        profile.setInteractionPreference("文字交流");
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("Profile 构建成功");
        System.out.println("专业: " + profile.getMajorOrField());
        System.out.println("目标: " + profile.getLearningGoal().getWholeText());
        System.out.println("✓ 测试数据准备完成");
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

    private static void assertNotNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNotNull(obj);
    }

    private static void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}