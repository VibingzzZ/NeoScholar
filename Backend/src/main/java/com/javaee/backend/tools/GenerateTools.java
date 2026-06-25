package com.javaee.backend.tools;

import com.javaee.backend.service.ResourceOrchestratorService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GenerateTools {

    @Lazy
    @Autowired
    private ResourceOrchestratorService resourceOrchestratorService;

    /**
     * 为指定学习路径生成配套的学习资源（PPT大纲、练习题等）
     * 当用户请求生成PPT或练习题时，调用此工具
     *
     * @param pathId 学习路径ID
     */
    @Tool("根据学习路径ID生成配套的学习资源，包括PPT大纲和练习题")
    public String generateLearningResource(Long pathId) {
        try {
            log.info("收到资源生成请求，路径ID: {}", pathId);
            resourceOrchestratorService.generateResource(pathId);
            String result = "学习资源生成成功！已为该路径下的所有节点生成了相应的PPT大纲和练习题。";
            log.info(result);
            return result;
        } catch (Exception e) {
            log.error("资源生成失败", e);
            return "资源生成失败: " + e.getMessage();
        }
    }
}