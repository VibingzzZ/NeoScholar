package com.javaee.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.config.Result;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.service.FileStorageService;
import com.javaee.backend.service.LearningPathService;
import com.javaee.backend.service.ResourceOrchestratorService;
import com.javaee.backend.service.ResourceRecommendationService;
import com.javaee.backend.po.dto.RecommendedResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习路径查询与管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/learning-path")
public class LearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ResourceOrchestratorService resourceOrchestratorService;

    @Autowired
    @Qualifier("resourceGenExecutor")
    private ExecutorService resourceGenExecutor;

    @Autowired
    private ResourceRecommendationService recommendationService;

    /**
     * 查询用户的所有学习路径
     */
    @GetMapping("/list/{userId}")
    public Result<List<LearningPaths>> listPaths(@PathVariable Long userId) {
        List<LearningPaths> paths = learningPathService.listByUserId(userId);
        return Result.success(paths);
    }

    /**
     * 查询路径详情
     */
    @GetMapping("/detail/{pathId}")
    public Result<LearningPaths> getPathDetail(@PathVariable Long pathId) {
        LearningPaths path = learningPathService.getById(pathId);
        if (path == null) {
            return Result.error("学习路径不存在");
        }
        return Result.success(path);
    }

    /**
     * 根据用户 ID 生成学习路径
     */
    @PostMapping("/generate/{userId}")
    public Result<Map<String, Object>> generatePath(@PathVariable Long userId) {
        log.info("收到学习路径生成请求, userId: {}", userId);
        Long pathId = learningPathService.generatePath(userId);
        if (pathId == null) {
            return Result.error("路径生成失败，请确认已创建学生画像");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("pathId", pathId);
        data.put("message", "路径生成成功");
        return Result.success(data);
    }

    /**
     * 更新学习进度
     */
    @PostMapping("/progress")
    public Result<Void> updateProgress(@RequestBody Map<String, Object> body) {
        Long pathId = ((Number) body.get("pathId")).longValue();
        Integer nodeIndex = ((Number) body.get("nodeIndex")).intValue();
        log.info("更新学习进度, pathId: {}, nodeIndex: {}", pathId, nodeIndex);
        learningPathService.updateProgress(pathId, nodeIndex);
        return Result.success();
    }

    /**
     * 查询学习路径下所有生成的资源（PPT、测验等）
     */
    @GetMapping("/{pathId}/resources")
    public Result<List<LearningResource>> getResources(@PathVariable Long pathId) {
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningResource::getPathId, pathId)
                .orderByAsc(LearningResource::getNodeIndex);
        List<LearningResource> resources = learningResourceMapper.selectList(wrapper);
        return Result.success(resources);
    }

    /**
     * 为学习路径生成配套资源（PPT大纲 + 练习题），异步执行避免超时。
     */
    @PostMapping("/{pathId}/generate-resources")
    public Result<String> generateResources(@PathVariable Long pathId) {
        log.info("收到资源生成请求（异步）, pathId: {}", pathId);
        CompletableFuture.runAsync(() -> {
            try {
                resourceOrchestratorService.generateResource(pathId);
                log.info("资源生成完成, pathId: {}", pathId);
            } catch (Exception e) {
                log.error("资源生成失败, pathId: {}", pathId, e);
            }
        }, resourceGenExecutor);
        return Result.success("资源生成任务已提交，正在后台处理，请稍后刷新查看");
    }

    /**
     * 基于画像的个性化资源推荐
     */
    @GetMapping("/recommend/{userId}")
    public Result<List<RecommendedResourceDTO>> recommendResources(
            @PathVariable Long userId,
            @RequestParam(required = false) Long pathId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("资源推荐请求, userId: {}, pathId: {}, limit: {}", userId, pathId, limit);
        List<RecommendedResourceDTO> recommendations = recommendationService.recommendResources(userId, pathId, limit);
        return Result.success(recommendations);
    }

    /**
     * 下载学习资源文件
     */
    @GetMapping("/resource/{resourceId}/download")
    public ResponseEntity<InputStreamResource> downloadResource(@PathVariable Long resourceId) {
        LearningResource resource = learningResourceMapper.selectById(resourceId);
        if (resource == null || resource.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = fileStorageService.getFilePath(resource.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            FileInputStream fis = new FileInputStream(filePath.toFile());
            InputStreamResource inputStreamResource = new InputStreamResource(fis);

            String encodedFileName = URLEncoder.encode(
                    resource.getTitle() + ".txt", StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(Files.size(filePath))
                    .body(inputStreamResource);
        } catch (FileNotFoundException e) {
            log.error("文件未找到: {}", filePath, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("下载文件失败: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
