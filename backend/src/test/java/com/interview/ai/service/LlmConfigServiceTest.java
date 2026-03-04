package com.interview.ai.service;

import com.interview.ai.model.LlmConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LlmConfigServiceTest {

    private LlmConfigService llmConfigService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        llmConfigService = new LlmConfigService(objectMapper);
        ReflectionTestUtils.setField(llmConfigService, "dataPath", tempDir.toString());
        llmConfigService.init();
    }

    @Test
    void testSaveAndGetAll() {
        LlmConfig config = LlmConfig.builder()
                .name("Test Model")
                .type(LlmConfig.ConfigType.OLLAMA)
                .modelType(LlmConfig.ModelType.CHAT)
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .active(true)
                .build();

        llmConfigService.saveConfig(config);

        List<LlmConfig> configs = llmConfigService.getAllConfigs();
        assertEquals(1, configs.size());
        assertEquals("Test Model", configs.get(0).getName());
    }

    @Test
    void testActivationLogic() {
        LlmConfig config1 = LlmConfig.builder()
                .id("1")
                .name("Model 1")
                .modelType(LlmConfig.ModelType.CHAT)
                .active(true)
                .build();
        LlmConfig config2 = LlmConfig.builder()
                .id("2")
                .name("Model 2")
                .modelType(LlmConfig.ModelType.CHAT)
                .active(false)
                .build();

        llmConfigService.saveConfig(config1);
        llmConfigService.saveConfig(config2);

        assertTrue(llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT).get().getName().equals("Model 1"));

        llmConfigService.setActive("2");

        Optional<LlmConfig> active = llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT);
        assertTrue(active.isPresent());
        assertEquals("Model 2", active.get().getName());

        List<LlmConfig> all = llmConfigService.getAllConfigs();
        LlmConfig c1 = all.stream().filter(c -> c.getId().equals("1")).findFirst().get();
        assertFalse(c1.isActive());
    }
}
