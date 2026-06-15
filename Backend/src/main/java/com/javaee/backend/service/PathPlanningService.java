package com.javaee.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.javaee.backend.entity.StudentProfile;

public interface PathPlanningService {

    /**
     * 生成并保存学习路径
     *
     * @param profile 学生画像
     * @return
     */
    Long generateAndSavePath(StudentProfile profile) throws JsonProcessingException;

}
