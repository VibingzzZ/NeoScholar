package com.javaee.backend.service;

import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class ResourceOrchestratorAITest {

    @Autowired
    private ResourceOrchestratorService orchestratorService;

    @Autowired
    private LearningPathsMapper learningPathsMapper;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @BeforeEach
    void checkApiKey() {
        String apiKey = System.getenv("QWEN_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isBlank(),
                "跳过：未设置 QWEN_API_KEY 环境变量");
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