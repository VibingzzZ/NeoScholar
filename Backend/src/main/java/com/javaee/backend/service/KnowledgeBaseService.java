package com.javaee.backend.service;

import java.util.List;
import java.util.Map;

/**
 * 课程知识库检索服务 —— 从课程 markdown 文档集中检索相关内容
 */
public interface KnowledgeBaseService {

    /**
     * 根据查询关键词检索相关课程内容
     * @param query 用户的问题或查询关键词
     * @param maxResults 最大返回条数
     * @return 相关文档片段列表，每项包含标题和内容
     */
    List<Map<String, String>> search(String query, int maxResults);

    /**
     * 获取知识库的课程名称和章节列表
     */
    Map<String, Object> getIndex();
}
