package com.javaee.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.PathPlanningAIService;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.PathPlanningMapper;
import com.javaee.backend.po.dto.LearningPathNodeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class PathPlanningAITest {

    @Autowired
    private PathPlanningService pathPlanningService;

    @Autowired
    private PathPlanningAIService pathPlanningAIService;

    @Autowired
    private PathPlanningMapper pathPlanningMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void checkApiKey() {
        String apiKey = System.getenv("QWEN_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isBlank(),
                "跳过：未设置 QWEN_API_KEY 环境变量");
    }

    /**
     * 测试LLM生成学习路径功能
     */
    @Test
    void testLLMPathGeneration() throws JsonProcessingException {
        System.out.println("=== 测试LLM路径生成 ===");

        String major = "计算机科学";
        String goal = "我想掌握机器学习和深度学习的基础知识";
        String knowledge = "Python, Java, 基础数学, 线性代数";

        // 调用LLM生成路径
        String pathJson = pathPlanningAIService.evaluatePath(major, goal, knowledge);
        System.out.println("LLM原始返回:\n" + pathJson);
        // 手动解析JSON为List
        List<LearningPathNodeDTO> pathNodes = objectMapper.readValue(pathJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, LearningPathNodeDTO.class));

        // 验证结果
        System.out.println("生成的学习路径节点数: " + (pathNodes != null ? pathNodes.size() : 0));

        if (pathNodes != null && !pathNodes.isEmpty()) {
            if (pathNodes.size() >= 5 && pathNodes.size() <= 8) {
                System.out.println("✓ 节点数量符合要求（5-8个）");
            } else {
                System.out.println("✗ 节点数量不符合要求，期望5-8个，实际: " + pathNodes.size());
            }
            // 打印每个节点的内容
            for (LearningPathNodeDTO node : pathNodes) {
                System.out.println("\n--- 节点 " + node.getStepOrder() + " ---");
                System.out.println("标题: " + node.getTitle());
                System.out.println("描述: " + node.getDescription());
                System.out.println("预计天数: " + node.getEstimatedDays());
            }
        } else {
            System.out.println("✗ LLM返回空路径");
        }
    }

    /**
     * 测试完整的路径生成和保存流程
     */
    @Test
    void testGenerateAndSavePath() {
        System.out.println("\n=== 测试路径生成并保存 ===");

        // 准备测试用的学生画像
        StudentProfile profile = new StudentProfile();
        profile.setId(1L);
        profile.setUserId(100L);
        profile.setMajorOrField("计算机科学");
        profile.setLearningGoal(createText("掌握人工智能基础"));
        profile.setKnowledgeBase(createText("Python, 数据结构, 算法"));
        profile.setCognitiveStyle("逻辑思维");
        profile.setInteractionPreference("文字交流");
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        try {
            // 调用服务生成并保存路径
            pathPlanningService.generateAndSavePath(profile);
            System.out.println("✓ 路径生成和保存成功");

            // 查询验证数据库记录
            List<LearningPaths> paths = pathPlanningMapper.selectList(null);
            if (paths != null && !paths.isEmpty()) {
                LearningPaths lastPath = paths.get(paths.size() - 1);
                System.out.println("\n保存的路径信息:");
                System.out.println("路径ID: " + lastPath.getId());
                System.out.println("用户ID: " + lastPath.getUserId());
                System.out.println("路径名称: " + lastPath.getPathName());
                System.out.println("节点JSON: " + lastPath.getNodesJson());
                System.out.println("当前节点索引: " + lastPath.getCurrentNodeIndex());
                System.out.println("状态: " + lastPath.getStatus());
            }
        } catch (Exception e) {
            System.out.println("✗ 路径生成失败: " + e.getMessage());
            e.printStackTrace();
        }
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