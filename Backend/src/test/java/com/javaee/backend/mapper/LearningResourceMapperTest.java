package com.javaee.backend.mapper;

import com.javaee.backend.entity.LearningResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LearningResourceMapperTest {

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Test
    void testInsertAndQuery() {
        System.out.println("=== 测试资源插入与查询 ===");

        LearningResource resource = new LearningResource();
        resource.setPathId(1L);
        resource.setNodeIndex(1);
        resource.setTitle("测试资源");
        resource.setContentText("这是测试内容");
        resource.setFilePath("/upload/test.txt");
        resource.setResourceType("ppt");
        resource.setGeneratedByAgent("PPTAgent");
        resource.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        int rows = learningResourceMapper.insert(resource);
        assertEquals(1, rows);
        assertNotNull(resource.getId(), "插入后应自动回填 ID");
        System.out.println("插入成功, ID: " + resource.getId());

        LearningResource found = learningResourceMapper.selectById(resource.getId());
        assertNotNull(found);
        assertEquals("测试资源", found.getTitle());
        assertEquals("PPTAgent", found.getGeneratedByAgent());
        System.out.println("查询成功: " + found.getTitle() + " by " + found.getGeneratedByAgent());

        learningResourceMapper.deleteById(resource.getId());
        System.out.println("清理完毕");
    }

    @Test
    void testQueryByPathId() {
        System.out.println("=== 测试按 pathId 查询资源 ===");

        List<LearningResource> resources = learningResourceMapper.selectList(
                new QueryWrapper<LearningResource>().eq("path_id", 1L));
        System.out.println("pathId=1 的资源数: " + resources.size());
        resources.forEach(r -> System.out.println("  - " + r.getTitle() + " (" + r.getGeneratedByAgent() + ")"));
    }

    @Test
    void testQueryByAgent() {
        System.out.println("=== 测试按 Agent 查询资源 ===");

        List<LearningResource> resources = learningResourceMapper.selectList(
                new QueryWrapper<LearningResource>().eq("generated_by_agent", "PPTAgent"));
        System.out.println("PPTAgent 生成的资源数: " + resources.size());
    }
}