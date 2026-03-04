package com.interview.ai.config;

import com.interview.ai.model.LlmConfig;
import com.interview.ai.model.McpConfig;
import com.interview.ai.service.LlmConfigService;
import com.interview.ai.service.McpConfigService;
import com.interview.ai.service.Interviewer;
import com.interview.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChatConfig {

    private final LlmConfigService llmConfigService;
    private final McpConfigService mcpConfigService;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                .map(this::createChatModel)
                .orElse(null);
    }

    private ChatLanguageModel createChatModel(LlmConfig config) {
        return dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .build();
    }

    @Bean
    public dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatLanguageModel() {
        return llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                .map(this::createStreamingChatModel)
                .orElse(null);
    }

    private dev.langchain4j.model.chat.StreamingChatLanguageModel createStreamingChatModel(LlmConfig config) {
        return dev.langchain4j.model.openai.OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .build();
    }

    @Bean
    @org.springframework.context.annotation.Lazy
    public Interviewer interviewer(dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatModel,
            RagService ragService) {
        if (streamingChatModel == null) {
            log.error("Cannot create Interviewer: StreamingChatLanguageModel is null");
            return null;
        }
        AiServices<Interviewer> builder = AiServices.builder(Interviewer.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(ragService.getContentRetriever());

        List<McpConfig> mcpConfigs = mcpConfigService.getAllConfigs();
        for (McpConfig mcpConfig : mcpConfigs) {
            try {
                dev.langchain4j.mcp.client.McpClient mcpClient = new dev.langchain4j.mcp.client.DefaultMcpClient.Builder()
                        .transport(new dev.langchain4j.mcp.client.transport.http.HttpMcpTransport.Builder()
                                .sseUrl(mcpConfig.getSseUrl())
                                .build())
                        .build();
                builder.tools(mcpClient.listTools());
                log.info("MCP tools integrated from {} ({})", mcpConfig.getName(), mcpConfig.getSseUrl());
            } catch (Exception e) {
                log.error("Failed to connect to MCP server: {}", mcpConfig.getName(), e);
            }
        }
        return builder.build();
    }
}
