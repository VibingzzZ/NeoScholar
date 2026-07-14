package com.javaee.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.config.Result;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.service.FileStorageService;
import com.javaee.backend.service.LearningPathService;
import lombok.extern.slf4j.Slf4j;
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
