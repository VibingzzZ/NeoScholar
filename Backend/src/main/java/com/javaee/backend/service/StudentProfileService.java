package com.javaee.backend.service;

import com.javaee.backend.entity.StudentProfile;

import java.util.List;

/**
 * 学生画像 CRUD 服务
 */
public interface StudentProfileService {

    /**
     * 根据 ID 查询画像
     */
    StudentProfile getById(Long id);

    /**
     * 查询某用户的所有画像
     */
    List<StudentProfile> listByUserId(Long userId);

    /**
     * 创建画像
     */
    StudentProfile create(StudentProfile profile);

    /**
     * 更新画像
     */
    StudentProfile update(StudentProfile profile);

    /**
     * 根据用户 ID 获取最新画像（用于路径生成等）
     */
    StudentProfile getLatestByUserId(Long userId);

    /**
     * 获取用户当前活跃画像
     */
    StudentProfile getActiveByUserId(Long userId);

    /**
     * 设置活跃画像（将指定画像设为活跃，其余置为非活跃）
     */
    void setActive(Long profileId, Long userId);
}
