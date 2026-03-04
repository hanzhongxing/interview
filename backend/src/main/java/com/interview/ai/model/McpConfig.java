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
public class McpConfig {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name;
    private String sseUrl;
}
