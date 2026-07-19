package com.javaee.backend.controller;

import com.javaee.backend.config.Result;
import com.javaee.backend.entity.ProfileMergeHistory;
import com.javaee.backend.mapper.ProfileMergeHistoryMapper;
import com.javaee.backend.service.ProfileMergeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/user/profile")
public class ProfileMergeController {

    @Autowired
    private ProfileMergeService profileMergeService;

    @Autowired
    private ProfileMergeHistoryMapper mergeHistoryMapper;

    @PostMapping("/merge/{id}/{userId}")
    public Result<String> mergeProfiles(
            @PathVariable Long id,
            @PathVariable Long userId) {

        log.info("收到合并请求，原始ID: {}, 新ID: {}", id, userId);
        profileMergeService.asyncExtractAndMergeProfile(id, userId);
        return Result.success("学生画像合并成功");
    }

    /**
     * 查询用户的合并历史
     */
    @GetMapping("/merge-history/{userId}")
    public Result<List<ProfileMergeHistory>> getMergeHistory(@PathVariable Long userId) {
        LambdaQueryWrapper<ProfileMergeHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProfileMergeHistory::getUserId, userId)
                .orderByDesc(ProfileMergeHistory::getMergedAt);
        List<ProfileMergeHistory> history = mergeHistoryMapper.selectList(wrapper);
        return Result.success(history);
    }
}