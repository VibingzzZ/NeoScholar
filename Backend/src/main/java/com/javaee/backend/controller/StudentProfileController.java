package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/user/profile")
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    @PostMapping("/detail/{id}")
    public Result<StudentProfile> getProfile(@PathVariable Long id) {
        StudentProfile profile = studentProfileService.getById(id);
        if (profile == null) {
            return Result.error("画像不存在");
        }
        return Result.success(profile);
    }

    @GetMapping("/list/{userId}")
    public Result<List<StudentProfile>> listProfiles(@PathVariable Long userId) {
        return Result.success(studentProfileService.listByUserId(userId));
    }

    @PostMapping("/create")
    public Result<StudentProfile> createProfile(@RequestBody StudentProfile profile) {
        log.info("创建学生画像, userId: {}", profile.getUserId());
        studentProfileService.create(profile);
        return Result.success(profile);
    }

    @PostMapping("/update")
    public Result<StudentProfile> updateProfile(@RequestBody StudentProfile profile) {
        log.info("更新学生画像, id: {}", profile.getId());
        studentProfileService.update(profile);
        return Result.success(profile);
    }
}
