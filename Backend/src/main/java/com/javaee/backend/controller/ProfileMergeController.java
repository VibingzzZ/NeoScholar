package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.service.ProfileMergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/profile")
@CrossOrigin(origins = "*")
public class ProfileMergeController {

    @Autowired
    private ProfileMergeService profileMergeService;

    @PostMapping("/merge/{id}/{userId}")
    public Result<String> mergeProfiles(
            @PathVariable Long id,
            @PathVariable Long userId) {
        
        try {
            log.info("收到合并请求，原始ID: {}, 新ID: {}", id, userId);
            profileMergeService.asyncExtractAndMergeProfile(id, userId);
            return Result.success("学生画像合并成功");
        } catch (Exception e) {
            log.error("合并学生画像失败", e);
            return Result.error("合并失败: " + e.getMessage());
        }
    }
}