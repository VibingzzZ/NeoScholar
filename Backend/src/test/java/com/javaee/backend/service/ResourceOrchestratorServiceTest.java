package com.javaee.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.po.dto.LearningPathNodeDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResourceOrchestratorServiceTest {

    @Autowired
    private ResourceOrchestratorService orchestratorService;

    @Autowired
    private LearningPathsMapper learningPathsMapper;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void testGenerateResource() throws Exception {
        System.out.println("=== 测试资源生成（需要有效的 path_id） ===");

        List<LearningPaths> paths = learningPathsMapper.selectList(
                new QueryWrapper<LearningPaths>().last("LIMIT 1"));

        if (paths.isEmpty()) {
            System.out.println("数据库中没有学习路径，跳过测试");
            return;
        }

        Long pathId = paths.get(0).getId();
        System.out.println("使用学习路径 ID: " + pathId);

        orchestratorService.generateResource(pathId);

        List<LearningResource> resources = learningResourceMapper.selectList(
                new QueryWrapper<LearningResource>().eq("path_id", pathId));
        System.out.println("生成资源数: " + resources.size());
        resources.forEach(r -> System.out.println("  - " + r.getTitle() + " by " + r.getGeneratedByAgent()));
    }
}