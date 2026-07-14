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
            String content;
            String agentName;
            if("ppt".equals(node.getResourceType())){
                content = pptAgent.generateOutline(node.getTitle(), node.getDescription());
                agentName = "PPTAgent";
            }else if("quiz".equals(node.getResourceType())){
                content = quizAgent.generateQuestion(node.getTitle(), node.getDescription());
                agentName = "QuizAgent";
            }else{
                continue;
            }
            String filePath = fileStorageService.save(content, node.getTitle() + ".txt");

            //写数据库
            LearningResource resource = new LearningResource();
            resource.setPathId(pathId);
            resource.setNodeIndex(node.getStepOrder());
            resource.setTitle(node.getTitle());
            resource.setContentText(content);
            resource.setFilePath(filePath);
            resource.setResourceType(node.getResourceType());
            resource.setGeneratedByAgent(agentName);
            learningResourceMapper.insert(resource);

            // 热力图计数：每生成一个 PPT/测验资源 +1
            activityLogService.incrementActivity(path.getUserId());
        }
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