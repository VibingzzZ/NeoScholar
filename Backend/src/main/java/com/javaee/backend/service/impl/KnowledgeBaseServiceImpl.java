package com.javaee.backend.service.impl;

import com.javaee.backend.service.KnowledgeBaseService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于关键词匹配的课程知识库检索实现。
 * 启动时加载 knowledge-base/ 下所有 markdown 文件，构建内存索引。
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    // 章节文件：文件路径 → 全文内容
    private final Map<String, String> docStore = new LinkedHashMap<>();
    // 关键字倒排索引：关键词 → 匹配的文档路径集合
    private final Map<String, Set<String>> keywordIndex = new HashMap<>();

    private static final String KB_PATH = "classpath:knowledge-base/java-programming/*.md";

    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(KB_PATH);
            Arrays.sort(resources, Comparator.comparing(r -> r.getFilename()));

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String content = readContent(resource);
                docStore.put(filename, content);
                indexDocument(filename, content);
                log.info("知识库加载: {} ({} 字符)", filename, content.length());
            }
            log.info("知识库初始化完成，共加载 {} 个章节，{} 个关键词索引",
                    docStore.size(), keywordIndex.size());
        } catch (Exception e) {
            log.error("知识库加载失败", e);
        }
    }

    @Override
    public List<Map<String, String>> search(String query, int maxResults) {
        if (query == null || query.isBlank() || docStore.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 提取查询中的关键词
        Set<String> queryWords = tokenize(query);

        // 2. 对每个文档打分
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String filename : docStore.keySet()) {
            double score = calculateScore(filename, queryWords);
            if (score > 0) {
                scores.put(filename, score);
            }
        }

        // 3. 按分数降序排序，取 top-N
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> {
                    String filename = entry.getKey();
                    String fullContent = docStore.get(filename);
                    String title = extractTitle(fullContent);
                    String snippet = extractRelevantSnippet(fullContent, queryWords, 300);

                    Map<String, String> result = new LinkedHashMap<>();
                    result.put("source", filename);
                    result.put("title", title);
                    result.put("snippet", snippet);
                    result.put("score", String.format("%.1f", entry.getValue()));
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getIndex() {
        Map<String, Object> index = new LinkedHashMap<>();
        index.put("courseName", "Java语言程序设计");
        index.put("totalChapters", docStore.size());
        List<Map<String, String>> chapters = new ArrayList<>();
        for (String filename : docStore.keySet()) {
            String title = extractTitle(docStore.get(filename));
            Map<String, String> ch = new LinkedHashMap<>();
            ch.put("file", filename);
            ch.put("title", title);
            chapters.add(ch);
        }
        index.put("chapters", chapters);
        return index;
    }

    // ========== 私有方法 ==========

    private String readContent(Resource resource) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private void indexDocument(String filename, String content) {
        Set<String> words = tokenize(content);
        for (String word : words) {
            keywordIndex.computeIfAbsent(word, k -> new HashSet<>()).add(filename);
        }
    }

    private Set<String> tokenize(String text) {
        // 中文：按常见分隔符切分，保留2-6字词组
        // 英文：转小写后按非字母数字切分
        Set<String> tokens = new HashSet<>();

        // 提取中文词组（2-6个连续汉字）
        String chinesePattern = "[\\u4e00-\\u9fa5]{2,6}";
        java.util.regex.Matcher cm = java.util.regex.Pattern.compile(chinesePattern).matcher(text);
        while (cm.find()) {
            tokens.add(cm.group());
        }

        // 提取英文单词（3个字母以上）
        String englishPattern = "[a-zA-Z]{3,}";
        java.util.regex.Matcher em = java.util.regex.Pattern.compile(englishPattern).matcher(text.toLowerCase());
        while (em.find()) {
            tokens.add(em.group());
        }

        return tokens;
    }

    private double calculateScore(String filename, Set<String> queryWords) {
        Set<String> docWords = keywordIndex.entrySet().stream()
                .filter(e -> e.getValue().contains(filename))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        long matchCount = queryWords.stream().filter(docWords::contains).count();
        if (matchCount == 0) return 0;

        // TF：关键词在文档中出现的频率
        double tf = (double) matchCount / Math.max(1, docWords.size());

        // IDF：包含关键词的文档数越少，权重越高
        double idfSum = queryWords.stream()
                .filter(keywordIndex::containsKey)
                .mapToDouble(w -> Math.log((double) docStore.size() / keywordIndex.get(w).size()))
                .sum();

        return tf * (1 + idfSum) * 100;
    }

    private String extractTitle(String content) {
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ") && !trimmed.contains("第1章") && !trimmed.contains("第2章")
                    && !trimmed.contains("第3章") && !trimmed.contains("第4章")
                    && !trimmed.contains("第5章") && !trimmed.contains("第6章") && !trimmed.contains("第7章")) {
                return trimmed.substring(2).trim();
            }
        }
        // 回退：取第一个 # 标题
        for (String line : content.split("\n")) {
            if (line.trim().startsWith("# ")) {
                return line.trim().substring(2).trim();
            }
        }
        return "未命名章节";
    }

    private String extractRelevantSnippet(String content, Set<String> queryWords, int maxLength) {
        String[] paragraphs = content.split("\n\n");
        // 找包含最多关键词的段落
        String best = "";
        int bestMatches = 0;
        for (String para : paragraphs) {
            if (para.trim().startsWith("#") || para.trim().isEmpty()) continue;
            int matches = 0;
            for (String word : queryWords) {
                if (para.contains(word)) matches++;
            }
            if (matches > bestMatches) {
                bestMatches = matches;
                best = para;
            }
        }
        if (best.isEmpty() && paragraphs.length > 0) {
            best = paragraphs[Math.min(1, paragraphs.length - 1)];
        }
        // 截断
        best = best.replaceAll("\n", " ").trim();
        if (best.length() > maxLength) {
            best = best.substring(0, maxLength) + "...";
        }
        return best;
    }
}
