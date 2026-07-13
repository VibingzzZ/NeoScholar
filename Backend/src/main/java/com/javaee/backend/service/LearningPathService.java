package com.javaee.backend.service;

import com.javaee.backend.entity.LearningPaths;
import java.util.List;

public interface LearningPathService {
    List<LearningPaths> listByUserId(Long userId);
    LearningPaths getById(Long pathId);
    void updateProgress(Long pathId, Integer nodeIndex);
    Long generatePath(Long userId);
}
