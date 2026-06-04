package com.javaee.backend.po.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MergedProfileDTO {
    @JsonProperty("majorOrField")
    private String majorOrField;
    
    @JsonProperty("learningGoal")
    private String learningGoal;
    
    @JsonProperty("knowledgeBase")
    private String knowledgeBase;
    
    @JsonProperty("cognitiveStyle")
    private String cognitiveStyle;
    
    @JsonProperty("commonMistakes")
    private String commonMistakes;
    
    @JsonProperty("interactionPreference")
    private String interactionPreference;
}