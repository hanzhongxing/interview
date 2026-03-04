package com.interview.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.ai.model.LlmConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LlmConfigService {

    private final ObjectMapper objectMapper;

    @Value("${interview.data-path:/opt/web/interview/data}")
    private String dataPath;

    private String llmConfigPath;

    @PostConstruct
    public void init() {
        this.llmConfigPath = dataPath + "/llm";
        File dir = new File(llmConfigPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<LlmConfig> getAllConfigs() {
        List<LlmConfig> configs = new ArrayList<>();
        try {
            Path path = Paths.get(llmConfigPath);
            if (Files.exists(path)) {
                Files.list(path)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                LlmConfig config = objectMapper.readValue(p.toFile(), LlmConfig.class);
                                configs.add(config);
                            } catch (IOException e) {
                                log.error("Failed to read llm config from {}", p, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Failed to list llm configs", e);
        }
        return configs;
    }

    public void saveConfig(LlmConfig config) {
        if (config.getId() == null || config.getId().isEmpty()) {
            config.setId(java.util.UUID.randomUUID().toString());
        }
        try {
            // If this is set to active, deactivate others of the same model type
            if (config.isActive()) {
                deactivateOthers(config.getModelType(), config.getId());
            }

            File file = new File(llmConfigPath, config.getId() + ".json");
            objectMapper.writeValue(file, config);
        } catch (IOException e) {
            log.error("Failed to save llm config", e);
            throw new RuntimeException("Failed to save LLM configuration", e);
        }
    }

    private void deactivateOthers(LlmConfig.ModelType modelType, String currentId) {
        getAllConfigs().stream()
                .filter(c -> c.getModelType() == modelType && !c.getId().equals(currentId) && c.isActive())
                .forEach(c -> {
                    c.setActive(false);
                    saveConfigInternal(c);
                });
    }

    private void saveConfigInternal(LlmConfig config) {
        try {
            File file = new File(llmConfigPath, config.getId() + ".json");
            objectMapper.writeValue(file, config);
        } catch (IOException e) {
            log.error("Failed to save llm config internal", e);
        }
    }

    public void deleteConfig(String id) {
        File file = new File(llmConfigPath, id + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public Optional<LlmConfig> getActiveConfig(LlmConfig.ModelType modelType) {
        return getAllConfigs().stream()
                .filter(c -> c.getModelType() == modelType && c.isActive())
                .findFirst();
    }

    public void setActive(String id) {
        List<LlmConfig> configs = getAllConfigs();
        Optional<LlmConfig> target = configs.stream().filter(c -> c.getId().equals(id)).findFirst();
        if (target.isPresent()) {
            LlmConfig config = target.get();
            config.setActive(true);
            saveConfig(config);
        }
    }
}
