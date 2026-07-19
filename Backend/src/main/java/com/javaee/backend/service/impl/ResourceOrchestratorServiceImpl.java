package com.javaee.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.PPTAgent;
import com.javaee.backend.AIService.QuizAgent;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.LearningResource;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.mapper.LearningResourceMapper;
import com.javaee.backend.po.dto.LearningPathNodeDTO;
import com.javaee.backend.service.ActivityLogService;
import com.javaee.backend.service.FileStorageService;
import com.javaee.backend.service.ResourceOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class ResourceOrchestratorServiceImpl implements ResourceOrchestratorService {
    @Autowired
    private PPTAgent pptAgent;
    @Autowired
    private QuizAgent quizAgent;
    @Autowired
    private LearningResourceMapper learningResourceMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private LearningPathsMapper learningPathsMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ActivityLogService activityLogService;

    @Override
    public void generateResource(Long pathId) throws IOException {
        LearningPaths path = learningPathsMapper.selectById(pathId);

        List<LearningPathNodeDTO> nodes = parseNodes(path.getNodesJson());
        for(LearningPathNodeDTO node : nodes){
            String type = node.getResourceType();
            // 如果节点没有 resourceType，默认两种都生成
            if (type == null || type.isBlank()) {
                generateSingleResource(pathId, node, "ppt", pptAgent.generateOutline(node.getTitle(), node.getDescription()), "PPTAgent", path.getUserId());
                generateSingleResource(pathId, node, "quiz", quizAgent.generateQuestion(node.getTitle(), node.getDescription()), "QuizAgent", path.getUserId());
            } else if("ppt".equals(type)){
                generateSingleResource(pathId, node, type, pptAgent.generateOutline(node.getTitle(), node.getDescription()), "PPTAgent", path.getUserId());
            }else if("quiz".equals(type)){
                generateSingleResource(pathId, node, type, quizAgent.generateQuestion(node.getTitle(), node.getDescription()), "QuizAgent", path.getUserId());
            }
        }
    }
    private void generateSingleResource(Long pathId, LearningPathNodeDTO node,
                                          String resourceType, String content,
                                          String agentName, Long userId) throws IOException {
        String suffix = "ppt".equals(resourceType) ? "_PPT大纲" : "_练习题";
        String filePath = fileStorageService.save(content, node.getTitle() + suffix + ".txt");

        LearningResource resource = new LearningResource();
        resource.setPathId(pathId);
        resource.setNodeIndex(node.getStepOrder());
        resource.setTitle(node.getTitle() + suffix);
        resource.setContentText(content);
        resource.setFilePath(filePath);
        resource.setResourceType(resourceType);
        resource.setGeneratedByAgent(agentName);
        learningResourceMapper.insert(resource);

        activityLogService.incrementActivity(userId);
    }

    private List<LearningPathNodeDTO> parseNodes(String nodeJson) {
        try {
            return objectMapper.readValue(nodeJson,
                    new TypeReference<>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("解析学习节点JSON失败：",e);
        }
    }

}