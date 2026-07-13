package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.ProfileMergeAIService;
import com.javaee.backend.po.dto.MergedProfileDTO;
import com.javaee.backend.po.dto.Profile;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class ProfileMergeServiceImpl extends ServiceImpl<ProfileMergeMapper, StudentProfile> implements com.javaee.backend.service.ProfileMergeService {

    @Autowired
    private ProfileMergeMapper profileMergeMapper;
    
    @Autowired
    private ProfileMergeAIService profileMergeService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExecutorService profileMergeExecutor;

    @Override
    //@Transactional
    //事务注解只在SpringAOP中实现，异步方法无效
    public void asyncExtractAndMergeProfile(Long id, Long userId) {
        log.info("开始合并学生画像，原始ID: {}, 新ID: {}", id, userId);


        CompletableFuture.runAsync(() -> {
        try {
            StudentProfile originalProfile = profileMergeMapper.selectById(id);
            StudentProfile newProfile = profileMergeMapper.selectById(userId);

            if (originalProfile == null || newProfile == null) {
                log.error("学生画像不存在，原始ID: {}, 新ID: {}", id, userId);
//                throw new RuntimeException("学生画像不存在");
                //不能使用throw，直接return结束当前任务即可
                return;
            }

            log.info("原始画像: {}", originalProfile);
            log.info("新画像: {}", newProfile);

            Profile original = new Profile(
                    getStringValue(originalProfile.getMajorOrField()),
                    getStringValue(originalProfile.getLearningGoal()),
                    getStringValue(originalProfile.getKnowledgeBase()),
                    getStringValue(originalProfile.getCognitiveStyle()),
                    getStringValue(originalProfile.getCommonMistakes()),
                    getStringValue(originalProfile.getInteractionPreference())
            );

            Profile newProf = new Profile(
                    getStringValue(newProfile.getMajorOrField()),
                    getStringValue(newProfile.getLearningGoal()),
                    getStringValue(newProfile.getKnowledgeBase()),
                    getStringValue(newProfile.getCognitiveStyle()),
                    getStringValue(newProfile.getCommonMistakes()),
                    getStringValue(newProfile.getInteractionPreference())
            );

            MergedProfileDTO mergedProfile = profileMergeService.mergeProfiles(original, newProf);

            log.info("LLM合并结果: {}", mergedProfile);

            StudentProfile updateProfile = new StudentProfile();
            updateProfile.setId(id);
            updateProfile.setUserId(originalProfile.getUserId());
            updateProfile.setMajorOrField(mergedProfile.getMajorOrField());
            updateProfile.setLearningGoal(mergedProfile.getLearningGoal());
            updateProfile.setKnowledgeBase(mergedProfile.getKnowledgeBase());
            updateProfile.setCognitiveStyle(mergedProfile.getCognitiveStyle());
            updateProfile.setCommonMistakes(mergedProfile.getCommonMistakes());
            updateProfile.setInteractionPreference(mergedProfile.getInteractionPreference());
            updateProfile.setUpdateAt(new Timestamp(System.currentTimeMillis()));

            int result = profileMergeMapper.updateById(updateProfile);
            if (result > 0) {
                log.info("学生画像合并成功，ID: {}", id);
            } else {
                log.error("学生画像合并失败，ID: {}", id);
//              throw new RuntimeException("学生画像合并失败");
            }

        } catch (Exception e) {
            log.error("合并学生画像时发生错误", e);
            //throw new RuntimeException("合并学生画像时发生错误: " + e.getMessage(), e);
        }
        },profileMergeExecutor);
    }

    private String getStringValue(String value) {
        return value != null ? value : "";
    }
}