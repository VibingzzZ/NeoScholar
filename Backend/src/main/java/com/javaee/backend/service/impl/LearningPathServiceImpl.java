package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.javaee.backend.entity.LearningPaths;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.LearningPathsMapper;
import com.javaee.backend.service.LearningPathService;
import com.javaee.backend.service.PathPlanningService;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LearningPathServiceImpl extends ServiceImpl<LearningPathsMapper, LearningPaths> implements LearningPathService {

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private PathPlanningService pathPlanningService;

    @Override
    public List<LearningPaths> listByUserId(Long userId) {
        LambdaQueryWrapper<LearningPaths> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPaths::getUserId, userId)
                .orderByDesc(LearningPaths::getUpdatedAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public LearningPaths getById(Long pathId) {
        return baseMapper.selectById(pathId);
    }

    @Override
    public void updateProgress(Long pathId, Integer nodeIndex) {
        LambdaUpdateWrapper<LearningPaths> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(LearningPaths::getId, pathId)
                .set(LearningPaths::getCurrentNodeIndex, nodeIndex);
        baseMapper.update(wrapper);
        log.info("更新学习路径进度成功, pathId: {}, nodeIndex: {}", pathId, nodeIndex);
    }

    @Override
    public Long generatePath(Long userId) {
        StudentProfile profile = studentProfileService.getLatestByUserId(userId);
        if (profile == null) {
            log.warn("未找到用户{}的学生画像，无法生成学习路径", userId);
            return null;
        }
        try {
            return pathPlanningService.generateAndSavePath(profile);
        } catch (Exception e) {
            log.error("生成学习路径失败, userId: {}", userId, e);
            return null;
        }
    }
}
