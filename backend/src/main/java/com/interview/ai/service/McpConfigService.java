package com.interview.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.ai.model.McpConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class McpConfigService {

    private final ObjectMapper objectMapper;

    @Value("${interview.data-path:/opt/web/interview/data}")
    private String dataPath;

    private String mcpFilePath;

    @PostConstruct
    public void init() {
        this.mcpFilePath = dataPath + "/mcp.json";
        File file = new File(mcpFilePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                objectMapper.writeValue(file, new ArrayList<McpConfig>());
            } catch (IOException e) {
                log.error("Failed to create mcp.json", e);
            }
        }
    }

    public List<McpConfig> getAllConfigs() {
        try {
            return objectMapper.readValue(new File(mcpFilePath), new TypeReference<List<McpConfig>>() {
            });
        } catch (IOException e) {
            log.error("Failed to read mcp configs", e);
            return new ArrayList<>();
        }
    }

    public void saveConfig(McpConfig config) {
        if (config.getId() == null || config.getId().isEmpty()) {
            config.setId(java.util.UUID.randomUUID().toString());
        }
        List<McpConfig> configs = getAllConfigs();
        configs.removeIf(c -> c.getId().equals(config.getId()));
        configs.add(config);
        saveAll(configs);
    }

    public void deleteConfig(String id) {
        List<McpConfig> configs = getAllConfigs();
        configs.removeIf(c -> c.getId().equals(id));
        saveAll(configs);
    }

    private void saveAll(List<McpConfig> configs) {
        try {
            objectMapper.writeValue(new File(mcpFilePath), configs);
        } catch (IOException e) {
            log.error("Failed to save mcp configs", e);
        }
    }
}
