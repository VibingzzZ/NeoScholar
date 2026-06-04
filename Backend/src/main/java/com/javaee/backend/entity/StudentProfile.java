package com.javaee.backend.entity;

import dev.langchain4j.internal.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentProfile {
    private Long id;
    private Long usreId;
    private String majorOrField;
    private Text learningGoal;
    private Text knowledgeBase;
    private String cognitiveStyle;
    private Json commonMistakes;
    private String interactionPreference;
    private Timestamp updateAt;

}
