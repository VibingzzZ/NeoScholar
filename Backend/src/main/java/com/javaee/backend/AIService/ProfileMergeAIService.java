package com.javaee.backend.AIService;

import com.javaee.backend.po.dto.MergedProfileDTO;
import com.javaee.backend.entity.Profile;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@SystemMessage(fromResource = "SystemMergeMsgPrompt.txt")
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface ProfileMergeAIService {

    @UserMessage(fromResource = "UserMergeMsgPrompt.txt")
    MergedProfileDTO mergeProfiles(
            @V("original") Profile original,
            @V("newProfile") Profile newProfile
    );
}