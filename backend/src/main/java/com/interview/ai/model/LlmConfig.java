package com.interview.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmConfig {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name;
    private ModelType modelType; // CHAT, VECTOR
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Double temperature;
    private boolean active;

    public enum ModelType {
        CHAT, VECTOR, SPEECH, TRANSCRIPTION
    }
}
