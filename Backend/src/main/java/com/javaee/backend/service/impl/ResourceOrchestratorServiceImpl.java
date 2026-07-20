package com.javaee.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.CodeCaseAgent;
import com.javaee.backend.AIService.MindMapAgent;
import com.javaee.backend.AIService.PPTAgent;
import com.javaee.backend.AIService.QuizAgent;
import com.javaee.backend.AIService.ReadingMaterialAgent;
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
    private MindMapAgent mindMapAgent;
    @Autowired
    private CodeCaseAgent codeCaseAgent;
    @Autowired
    private ReadingMaterialAgent readingMaterialAgent;
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
            // 如果节点没有 resourceType，默认生成所有支持的类型
            if (type == null || type.isBlank()) {
                generateSingleResource(pathId, node, "ppt", pptAgent.generateOutline(node.getTitle(), node.getDescription()), "PPTAgent", "_PPT大纲", ".md", path.getUserId());
                generateSingleResource(pathId, node, "quiz", quizAgent.generateQuestion(node.getTitle(), node.getDescription()), "QuizAgent", "_练习题", ".md", path.getUserId());
                generateSingleResource(pathId, node, "mindmap", mindMapAgent.generateMindMap(node.getTitle(), node.getDescription()), "MindMapAgent", "_思维导图", ".md", path.getUserId());
                generateSingleResource(pathId, node, "codecase", codeCaseAgent.generateCodeCase(node.getTitle(), node.getDescription()), "CodeCaseAgent", "_实操案例", ".md", path.getUserId());
                generateSingleResource(pathId, node, "reading", readingMaterialAgent.generateReadingMaterial(node.getTitle(), node.getDescription()), "ReadingMaterialAgent", "_拓展阅读", ".md", path.getUserId());
            } else {
                switch (type) {
                    case "ppt" -> generateSingleResource(pathId, node, type, pptAgent.generateOutline(node.getTitle(), node.getDescription()), "PPTAgent", "_PPT大纲", ".md", path.getUserId());
                    case "quiz" -> generateSingleResource(pathId, node, type, quizAgent.generateQuestion(node.getTitle(), node.getDescription()), "QuizAgent", "_练习题", ".md", path.getUserId());
                    case "mindmap" -> generateSingleResource(pathId, node, type, mindMapAgent.generateMindMap(node.getTitle(), node.getDescription()), "MindMapAgent", "_思维导图", ".md", path.getUserId());
                    case "codecase" -> generateSingleResource(pathId, node, type, codeCaseAgent.generateCodeCase(node.getTitle(), node.getDescription()), "CodeCaseAgent", "_实操案例", ".md", path.getUserId());
                    case "reading" -> generateSingleResource(pathId, node, type, readingMaterialAgent.generateReadingMaterial(node.getTitle(), node.getDescription()), "ReadingMaterialAgent", "_拓展阅读", ".md", path.getUserId());
                }
            }
        }
    }
    private void generateSingleResource(Long pathId, LearningPathNodeDTO node,
                                          String resourceType, String content,
                                          String agentName, String titleSuffix,
                                          String fileExt, Long userId) throws IOException {
        String filePath = fileStorageService.save(content, node.getTitle() + titleSuffix + fileExt);

        LearningResource resource = new LearningResource();
        resource.setPathId(pathId);
        resource.setNodeIndex(node.getStepOrder());
        resource.setTitle(node.getTitle() + titleSuffix);
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