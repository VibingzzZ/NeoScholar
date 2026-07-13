package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.service.LearningPathService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
