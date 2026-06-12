package com.javaee.backend.AIService;

import com.javaee.backend.po.dto.MergedProfileDTO;
import com.javaee.backend.po.dto.Profile;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@SystemMessage(fromResource = "SystemMergeMsgPrompt.txt")
@AiService
public interface ProfileMergeAIService {

    @UserMessage(fromResource = "UserMergeMsgPrompt.txt")
    MergedProfileDTO mergeProfiles(
            @V("original") Profile original,
            @V("newProfile") Profile newProfile
    );
}