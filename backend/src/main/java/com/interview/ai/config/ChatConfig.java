package com.interview.ai.config;

import com.interview.ai.service.Interviewer;
import com.interview.ai.service.RagService;
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
public class ChatConfig {

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name}")
    private String modelName;

    @Value("${interview.mode:ollama}")
    private String interviewMode;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String openAiApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o}")
    private String openAiModelName;

    @Value("${interview.mcp-server-url:}")
    private String mcpServerUrl;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("openai".equalsIgnoreCase(interviewMode)) {
            return dev.langchain4j.model.openai.OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .build();
        }
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .build();
    }

    @Bean
    public dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatLanguageModel() {
        if ("openai".equalsIgnoreCase(interviewMode)) {
            return dev.langchain4j.model.openai.OpenAiStreamingChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .build();
        }
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Bean
    public Interviewer interviewer(dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatModel,
            RagService ragService) {
        AiServices<Interviewer> builder = AiServices.builder(Interviewer.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(ragService.getContentRetriever());

        if (mcpServerUrl != null && !mcpServerUrl.isEmpty()) {
            try {
                dev.langchain4j.mcp.client.McpClient mcpClient =new  dev.langchain4j.mcp.client.DefaultMcpClient.Builder()
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
