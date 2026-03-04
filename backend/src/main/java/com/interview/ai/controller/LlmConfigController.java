package com.interview.ai.controller;

import com.interview.ai.model.LlmConfig;
import com.interview.ai.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/llm-configs")
@RequiredArgsConstructor
@CrossOrigin
public class LlmConfigController {

    private final LlmConfigService llmConfigService;

    @GetMapping
    public List<LlmConfig> getAllConfigs() {
        return llmConfigService.getAllConfigs();
    }

    @PostMapping
    public void saveConfig(@RequestBody LlmConfig config) {
        llmConfigService.saveConfig(config);
    }

    @DeleteMapping("/{id}")
    public void deleteConfig(@PathVariable String id) {
        llmConfigService.deleteConfig(id);
    }

    @PostMapping("/{id}/activate")
    public void activateConfig(@PathVariable String id) {
        llmConfigService.setActive(id);
    }
}
