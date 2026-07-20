package com.javaee.backend.tools;

import com.javaee.backend.service.KnowledgeBaseService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程知识库检索工具 — AI 在回答课程相关问题时，调用此工具检索权威课程内容，
 * 避免凭空编造知识点，有效防止"幻觉"。
 */
@Slf4j
@Service("KnowledgeSearchTools")
public class KnowledgeSearchTools {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 检索 Java 课程知识库，获取与问题相关的权威课程内容。
     * AI 应基于检索结果回答，不得编造课程中不存在的内容。
     *
     * @param query 搜索关键词或问题
     * @return 相关知识库条目的摘要
     */
    @Tool("搜索「Java语言程序设计」课程知识库。当你需要回答关于 Java 编程、面向对象、集合框架、多线程、IO流、异常处理、数据类型、语法规则等课程问题时，先调用此工具检索课程中的权威内容，再基于检索结果回答。如果知识库中没有相关内容，请明确告知用户「课程知识库中暂未收录此内容」。")
    public String searchKnowledgeBase(String query) {
        log.info("知识库检索: {}", query);
        List<Map<String, String>> results = knowledgeBaseService.search(query, 5);

        if (results.isEmpty()) {
            return "课程知识库中未找到与「" + query + "」直接相关的内容。请基于通用 Java 知识回答，并告知用户此内容不在当前课程知识库中。";
        }

        return results.stream()
                .map(r -> String.format("【%s】(来源:%s, 相关度:%s)\n%s",
                        r.get("title"), r.get("source"), r.get("score"), r.get("snippet")))
                .collect(Collectors.joining("\n---\n"));
    }
}
