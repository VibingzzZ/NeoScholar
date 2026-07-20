package com.javaee.backend.service;

/**
 * 内容安全审核服务接口
 * 负责对用户输入进行安全审核，过滤不当内容
 */
public interface ContentSafetyService {

    /**
     * 检查内容是否安全
     *
     * @param content 用户输入的内容
     * @return true 表示安全，false 表示包含不当内容
     */
    boolean isSafe(String content);
}
