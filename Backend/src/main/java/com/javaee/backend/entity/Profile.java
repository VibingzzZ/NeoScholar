package com.javaee.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private String major;
    private String learningGoal;
    private String knowledgeBase;
    private String cognitiveStyle;
    private String commonMistakes;
    private String interactionPreference;
}