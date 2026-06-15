package com.javaee.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.javaee.backend.config.Result;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.service.PathPlanningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/path")
public class PathPlanningController {

    @Autowired
    private PathPlanningService pathPlanningService;

    /**
     * 生成专属学习路径
     * @param profile 学生画像（前端传入）
     * @return 操作结果
     */
    @PostMapping("generate")
    public Result<Map<String, Object>> generatePath(@RequestBody StudentProfile profile) throws JsonProcessingException {
        log.info("收到学习路径生成请求, 用户ID: {}", profile.getUserId());
        Long pathId = pathPlanningService.generateAndSavePath(profile);
        if (pathId == null) {
            return Result.error("路径生成失败，请稍后重试");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("pathId", pathId);
        data.put("message", "路径生成成功");
        return Result.success(data);
    }
}