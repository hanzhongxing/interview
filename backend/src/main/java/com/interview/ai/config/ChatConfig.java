package com.interview.ai.config;

import com.interview.ai.model.LlmConfig;
import com.interview.ai.service.LlmConfigService;
import com.interview.ai.service.Interviewer;
import com.interview.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import java.time.Duration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChatConfig {

    private final LlmConfigService llmConfigService;

    @Value("${interview.mcp-server-url:}")
    private String mcpServerUrl;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                .map(this::createChatModel)
                .orElseGet(() -> {
                    log.warn("No active chat model found, providing a placeholder or failing gracefully");
                    return null; // Or throw an exception
                });
    }

    private ChatLanguageModel createChatModel(LlmConfig config) {
        if (config.getType() == LlmConfig.ConfigType.OPENAI) {
            return dev.langchain4j.model.openai.OpenAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature())
                    .build();
        } else {
            return OllamaChatModel.builder()
                    .baseUrl(config.getBaseUrl())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                    .build();
        }
    }

    @Bean
    public dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatLanguageModel() {
        return llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                .map(this::createStreamingChatModel)
                .orElseGet(() -> {
                    log.warn("No active streaming chat model found");
                    return null;
                });
    }

    private dev.langchain4j.model.chat.StreamingChatLanguageModel createStreamingChatModel(LlmConfig config) {
        if (config.getType() == LlmConfig.ConfigType.OPENAI) {
            return dev.langchain4j.model.openai.OpenAiStreamingChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature())
                    .build();
        } else {
            return OllamaStreamingChatModel.builder()
                    .baseUrl(config.getBaseUrl())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                    .timeout(Duration.ofMinutes(2))
                    .build();
        }
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

        if (mcpServerUrl != null && !mcpServerUrl.isEmpty()) {
            try {
                dev.langchain4j.mcp.client.McpClient mcpClient = new dev.langchain4j.mcp.client.DefaultMcpClient.Builder()
                        .transport(new dev.langchain4j.mcp.client.transport.http.HttpMcpTransport.Builder()
                                .sseUrl(mcpServerUrl)
                                .build())
                        .build();
                builder.tools(mcpClient.listTools());
                log.info("MCP tools integrated from {}", mcpServerUrl);
            } catch (Exception e) {
                log.error("Failed to connect to MCP server", e);
            }
        }

        return builder.build();
    }
}
