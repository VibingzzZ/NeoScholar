package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.backend.AIService.PathPlanningAIService;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.PathPlanningMapper;
import com.javaee.backend.po.dto.LearningPathNodeDTO;
import com.javaee.backend.service.PathPlanningService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class PathPlanningServiceImpl extends ServiceImpl<PathPlanningMapper, LearningPaths> implements PathPlanningService {

    @Autowired
    private PathPlanningMapper pathPlanningMapper;

    @Autowired
    private PathPlanningAIService pathPlanningAIService;


    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成学习路径并保存
     *
     * @param profile 学生信息
     * @return
     */
    @Override
    public Long generateAndSavePath(StudentProfile profile) {
        //判断学生档案是否为空
        if (profile == null) {
            log.warn("学生档案为空,无法生成学生路径");
            return null;
        }

        LearningPaths path = null;
        try {
            String major = profile.getMajorOrField();
            String goal = profile.getLearningGoal() != null ?
                    profile.getLearningGoal() : "";
            String knowledge = profile.getKnowledgeBase() != null ?
                    profile.getKnowledgeBase() : "";

            //调用大模型生成路径

            String pathJson = pathPlanningAIService.evaluatePath(major, goal, knowledge);
            if (pathJson == null || pathJson.trim().isEmpty()) {
                log.warn("大模型生成的学习路径为空,无法保存");
                return null;
            }
            // 手动解析JSON为List
            List<LearningPathNodeDTO> pathNodes = objectMapper.readValue(pathJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LearningPathNodeDTO.class));

            if (pathNodes.isEmpty()) {
                log.warn("大模型生成的学习路径节点为空,无法保存");
                return null;
            }
            String jsonStr = objectMapper.writeValueAsString(pathNodes);
            path = new LearningPaths();
            path.setUserId(profile.getUserId());
            path.setProfileId(profile.getId());
            path.setPathName(goal + "_专属学习路径");
            path.setNodesJson(jsonStr);
            path.setCurrentNodeIndex(0);
            path.setStatus(1);
            path.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // 保存路径到数据库
            pathPlanningMapper.insert(path);
            log.info("成功为用户{}生成包含{}个节点的专属学习路径", profile.getUserId(), pathNodes.size());
            // 返回生成的路径ID
            return path.getId();
        } catch (Exception e) {
            log.error("生成学习路径时发生错误", e);
        }
        return null;
    }
}