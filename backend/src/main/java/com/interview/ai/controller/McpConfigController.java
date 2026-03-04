package com.interview.ai.controller;

import com.interview.ai.model.McpConfig;
import com.interview.ai.service.McpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp-configs")
@RequiredArgsConstructor
@CrossOrigin
public class McpConfigController {

    private final McpConfigService mcpConfigService;

    @GetMapping
    public List<McpConfig> getAllConfigs() {
        return mcpConfigService.getAllConfigs();
    }

    @PostMapping
    public void saveConfig(@RequestBody McpConfig config) {
        mcpConfigService.saveConfig(config);
    }

    @DeleteMapping("/{id}")
    public void deleteConfig(@PathVariable String id) {
        mcpConfigService.deleteConfig(id);
    }
}
