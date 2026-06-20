package com.javaee.backend.service;

import com.javaee.backend.AIService.ProfileMergeAIService;
import com.javaee.backend.po.dto.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProfileMergeServiceAITest {

    @Autowired
    private ProfileMergeAIService profileMergeAIService;

    @Test
    void testLLMMerge() {
        Profile original = new Profile(
                "计算机科学",
                "我想掌握机器学习和深度学习的基础知识",
                "Python, Java, 基础数学",
                "逻辑思维",
                "{\"math\": \"代数错误\", \"coding\": \"语法错误\"}",
                "文字交流"
        );

        Profile newProfile = new Profile(
                "计算机科学",
                "我想掌握机器学习和深度学习",
                "Python, Java, 线性代数",
                "逻辑思维",
                "{\"math\": \"代数错误\", \"coding\": \"语法错误\"}",
                "文字交流"
        );

        String result = String.valueOf(profileMergeAIService.mergeProfiles(original, newProfile));

        System.out.println("LLM合并结果:");
        System.out.println(result);
    }
}