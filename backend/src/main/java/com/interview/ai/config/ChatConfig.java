package com.interview.ai.config;

import com.interview.ai.model.LlmConfig;
import com.interview.ai.model.McpConfig;
import com.interview.ai.service.LlmConfigService;
import com.interview.ai.service.McpConfigService;
import com.interview.ai.service.Interviewer;
import com.interview.ai.service.RagService;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import lombok.RequiredArgsConstructor;
import com.interview.ai.model.lazy.LazyChatLanguageModel;
import com.interview.ai.model.lazy.LazyStreamingChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatConfig {

        private final static int max_memory_num=100;

        private final LlmConfigService llmConfigService;
        private final McpConfigService mcpConfigService;

        @Bean
        public ChatLanguageModel chatLanguageModel() {
                return new LazyChatLanguageModel(
                                () -> llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                                                .map(this::createChatModel)
                                                .orElse(null));
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
        public StreamingChatLanguageModel streamingChatLanguageModel() {
                return new LazyStreamingChatLanguageModel(
                                () -> llmConfigService.getActiveConfig(LlmConfig.ModelType.CHAT)
                                                .map(this::createStreamingChatModel)
                                                .orElse(null));
        }

        private StreamingChatLanguageModel createStreamingChatModel(LlmConfig config) {
                return dev.langchain4j.model.openai.OpenAiStreamingChatModel.builder()
                                .baseUrl(config.getBaseUrl())
                                .apiKey(config.getApiKey())
                                .modelName(config.getModelName())
                                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                                .build();
        }

        @Bean
        @Lazy
        public Interviewer interviewer(StreamingChatLanguageModel streamingChatModel,RagService ragService) {
                AiServices<Interviewer> builder = AiServices.builder(Interviewer.class)
                                .streamingChatLanguageModel(streamingChatModel)
                                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(max_memory_num))
                                .contentRetriever(ragService.getContentRetriever());

                List<McpConfig> mcpConfigs = mcpConfigService.getAllConfigs();
                for (McpConfig mcpConfig : mcpConfigs) {
                        try {
                        McpClient mcpClient = new DefaultMcpClient.Builder()
                                .transport(new HttpMcpTransport.Builder()
                                                .sseUrl(mcpConfig.getSseUrl())
                                                .build())
                                .build();
                        builder.tools(mcpClient.listTools());
                        log.info("MCP tools integrated from {} ({})", mcpConfig.getName(),mcpConfig.getSseUrl());
                        } catch (Exception e) {
                                log.error("Failed to connect to MCP server: {}", mcpConfig.getName(), e);
                        }
                }
                return builder.build();
        }
}
