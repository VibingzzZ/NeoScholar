package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生画像 CRUD 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user/profile")
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    /**
     * 查询画像详情
     */
    @GetMapping("/detail/{id}")
    public Result<StudentProfile> getProfile(@PathVariable Long id) {
        StudentProfile profile = studentProfileService.getById(id);
        if (profile == null) {
            return Result.error("画像不存在");
        }
        return Result.success(profile);
    }

    /**
     * 查询用户的所有画像列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<StudentProfile>> listProfiles(@PathVariable Long userId) {
        List<StudentProfile> profiles = studentProfileService.listByUserId(userId);
        return Result.success(profiles);
    }

    /**
     * 创建画像
     */
    @PostMapping("/create")
    public Result<StudentProfile> createProfile(@RequestBody StudentProfile profile) {
        log.info("创建学生画像, userId: {}", profile.getUserId());
        studentProfileService.create(profile);
        return Result.success(profile);
    }

    /**
     * 更新画像
     */
    @PutMapping("/update")
    public Result<StudentProfile> updateProfile(@RequestBody StudentProfile profile) {
        log.info("更新学生画像, id: {}", profile.getId());
        studentProfileService.update(profile);
        return Result.success(profile);
    }
}
