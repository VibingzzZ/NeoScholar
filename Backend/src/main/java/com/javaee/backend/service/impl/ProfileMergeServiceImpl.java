package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.ProfileMergeAIService;
import com.javaee.backend.po.dto.MergedProfileDTO;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import dev.langchain4j.internal.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Slf4j
@Service
public class ProfileMergeServiceImpl extends ServiceImpl<ProfileMergeMapper, StudentProfile> implements com.javaee.backend.service.ProfileMergeService {

    @Autowired
    private ProfileMergeMapper profileMergeMapper;
    
    @Autowired
    private ProfileMergeAIService profileMergeService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void mergeProfile(Long id, Long userId) {
        log.info("开始合并学生画像，原始ID: {}, 新ID: {}", id, userId);
        
        try {
            StudentProfile originalProfile = profileMergeMapper.selectById(id);
            StudentProfile newProfile = profileMergeMapper.selectById(userId);

            if (originalProfile == null || newProfile == null) {
                log.error("学生画像不存在，原始ID: {}, 新ID: {}", id, userId);
                throw new RuntimeException("学生画像不存在");
            }

            log.info("原始画像: {}", originalProfile);
            log.info("新画像: {}", newProfile);

            String mergedResult = profileMergeService.mergeProfiles(
                    getStringValue(originalProfile.getMajorOrField()),
                    getTextValue(originalProfile.getLearningGoal()),
                    getTextValue(originalProfile.getKnowledgeBase()),
                    getStringValue(originalProfile.getCognitiveStyle()),
                    getJsonValue(originalProfile.getCommonMistakes()),
                    getStringValue(originalProfile.getInteractionPreference()),
                    getStringValue(newProfile.getMajorOrField()),
                    getTextValue(newProfile.getLearningGoal()),
                    getTextValue(newProfile.getKnowledgeBase()),
                    getStringValue(newProfile.getCognitiveStyle()),
                    getJsonValue(newProfile.getCommonMistakes()),
                    getStringValue(newProfile.getInteractionPreference())
            );

            log.info("LLM合并结果: {}", mergedResult);

            MergedProfileDTO mergedProfile = objectMapper.readValue(mergedResult, MergedProfileDTO.class);
            log.info("解析后的合并画像: {}", mergedProfile);

            StudentProfile updateProfile = new StudentProfile();
            updateProfile.setId(id);
            updateProfile.setUserId(originalProfile.getUserId());
            updateProfile.setMajorOrField(mergedProfile.getMajorOrField());
            updateProfile.setLearningGoal(createText(mergedProfile.getLearningGoal()));
            updateProfile.setKnowledgeBase(createText(mergedProfile.getKnowledgeBase()));
            updateProfile.setCognitiveStyle(mergedProfile.getCognitiveStyle());
            updateProfile.setCommonMistakes(createJson(mergedProfile.getCommonMistakes()));
            updateProfile.setInteractionPreference(mergedProfile.getInteractionPreference());
            updateProfile.setUpdateAt(new java.sql.Timestamp(System.currentTimeMillis()));

            int result = profileMergeMapper.updateById(updateProfile);
            if (result > 0) {
                log.info("学生画像合并成功，ID: {}", id);
            } else {
                log.error("学生画像合并失败，ID: {}", id);
                throw new RuntimeException("学生画像合并失败");
            }

        } catch (Exception e) {
            log.error("合并学生画像时发生错误", e);
            throw new RuntimeException("合并学生画像时发生错误: " + e.getMessage(), e);
        }
    }

    private String getStringValue(String value) {
        return value != null ? value : "";
    }

    private String getTextValue(Text text) {
        return text != null ? text.getData() : "";
    }

    private String getJsonValue(dev.langchain4j.internal.Json json) {
        return json != null ? json.toString() : "{}";
    }

    private Text createText(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (Exception e) {
            log.error("创建Text对象时发生错误", e);
            return null;
        }
        return doc.createTextNode(content);
    }

    private Json createJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, Json.class);
        } catch (Exception e) {
            log.error("创建Json对象时发生错误: {}", jsonString, e);
            return null;
        }
    }
}