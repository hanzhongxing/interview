package com.interview.ai.controller;

import com.interview.ai.service.LlmConfigService;
import com.interview.ai.service.McpConfigService;
import com.interview.ai.model.LlmConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
@CrossOrigin
public class SystemStatusController {

    private final LlmConfigService llmConfigService;
    private final McpConfigService mcpConfigService;

    @GetMapping
    public Map<String, Object> getStatus() {
        boolean llmConfigured = llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT).isPresent();
        int mcpCount = mcpConfigService.getAllConfigs().size();

        return Map.of(
                "llmConfigured", llmConfigured,
                "mcpCount", mcpCount);
    }
}
