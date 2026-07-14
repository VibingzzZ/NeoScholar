package com.javaee.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.po.dto.LearningPathNodeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathPlanning 服务单元测试（不含 AI 调用）
 * AI 集成测试参见 {@link PathPlanningAIIT}
 */
@SpringBootTest
class PathPlanningServiceTest {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试 nodesJson 解析逻辑
     */
    @Test
    void testParseNodesJson() throws Exception {
        System.out.println("=== 测试 nodesJson 解析 ===");

        String json = """
            [
                {"stepOrder":1, "title":"微积分基础", "description":"学习极限与导数", "estimatedDays":3, "resourceType":"ppt"},
                {"stepOrder":2, "title":"导数练习", "description":"巩固求导计算", "estimatedDays":2, "resourceType":"quiz"}
            ]""";

        List<LearningPathNodeDTO> nodes = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, LearningPathNodeDTO.class));

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("微积分基础", nodes.get(0).getTitle());
        assertEquals("ppt", nodes.get(0).getResourceType());
        assertEquals("quiz", nodes.get(1).getResourceType());

        System.out.println("节点数: " + nodes.size());
        nodes.forEach(n -> System.out.println("  - " + n.getTitle() + " [" + n.getResourceType() + "]"));
    }
}