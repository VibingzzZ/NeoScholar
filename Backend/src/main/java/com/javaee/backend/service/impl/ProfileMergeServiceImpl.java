package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.ProfileMergeAIService;
import com.javaee.backend.po.dto.MergedProfileDTO;
import com.javaee.backend.entity.Profile;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Lazy
    @Autowired
    private ProfileMergeServiceImpl self;

    @Override
    public void asyncExtractAndMergeProfile(Long id, Long userId) {
        log.info("开始合并学生画像，原始ID: {}, 新ID: {}", id, userId);

        CompletableFuture.runAsync(() -> {
        try {
            StudentProfile originalProfile = profileMergeMapper.selectById(id);
            StudentProfile newProfile = profileMergeMapper.selectById(userId);

            if (originalProfile == null || newProfile == null) {
                log.error("学生画像不存在，原始ID: {}, 新ID: {}", id, userId);
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

            // 通过 self 代理调用事务方法，确保 DB 更新有事务保护
            self.saveMergedProfile(updateProfile, id);

        } catch (Exception e) {
            log.error("合并学生画像时发生错误", e);
        }
        }, profileMergeExecutor);
    }

    /**
     * 事务保护的画像保存方法。
     * 必须通过 self 代理调用（而非 this），否则 @Transactional 不生效。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMergedProfile(StudentProfile updateProfile, Long id) {
        int result = profileMergeMapper.updateById(updateProfile);
        if (result > 0) {
            log.info("学生画像合并成功，ID: {}", id);
        } else {
            log.error("学生画像合并失败，ID: {}", id);
            throw new RuntimeException("学生画像合并失败");
        }
    }

    private String getStringValue(String value) {
        return value != null ? value : "";
    }
}