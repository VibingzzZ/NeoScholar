package com.javaee.backend.AIService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@SystemMessage(fromResource = "SystemMergeMsgPrompt.txt")
@AiService
public interface ProfileMergeAIService {

    @UserMessage(fromResource = "UserMergeMsgPrompt.txt")
    String mergeProfiles(
            @V("originalMajor") String originalMajor,
            @V("originalLearningGoal") String originalLearningGoal,
            @V("originalKnowledgeBase") String originalKnowledgeBase,
            @V("originalCognitiveStyle") String originalCognitiveStyle,
            @V("originalCommonMistakes") String originalCommonMistakes,
            @V("originalInteractionPreference") String originalInteractionPreference,
            @V("newMajor") String newMajor,
            @V("newLearningGoal") String newLearningGoal,
            @V("newKnowledgeBase") String newKnowledgeBase,
            @V("newCognitiveStyle") String newCognitiveStyle,
            @V("newCommonMistakes") String newCommonMistakes,
            @V("newInteractionPreference") String newInteractionPreference
    );
}