package com.javaee.backend.service;



public interface ProfileMergeService  {

    /**
     * 合并学生的个人信息画像
     * @param id 学生ID
     * @param userId 用户ID
     */
    void mergeProfile(Long id, Long userId);
}
