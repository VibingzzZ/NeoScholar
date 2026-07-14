package com.javaee.backend.service;

/**
 * 用户活动记录服务（热力图数据来源）
 */
public interface ActivityLogService {

    /**
     * 为用户当日活动计数 +1
     * 如果今日已有记录则 count+1，否则新增 count=1 的记录
     */
    void incrementActivity(Long userId);
}
