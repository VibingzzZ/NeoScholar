package com.javaee.backend.service;



public interface ProfileMergeService  {

    /**
     * 合并学生的个人信息画像
     * @param id 学生ID
     * @param userId 用户ID
     */
  //  void mergeProfile(Long id, Long userId);

    /**
     * 异步提取学生的个人信息并合并
     * @param id 学生ID
     * @param userId 用户ID
     */
    void asyncExtractAndMergeProfile(Long id, Long userId);
}
