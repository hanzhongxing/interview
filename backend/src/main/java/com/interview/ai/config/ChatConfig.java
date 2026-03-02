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

import java.time.Duration;

@Configuration
public class ChatConfig {

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name}")
    private String modelName;

    @Bean
    public OllamaStreamingChatModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Bean
    public Interviewer interviewer(OllamaStreamingChatModel streamingChatModel, RagService ragService) {
        return AiServices.builder(Interviewer.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(ragService.getContentRetriever())
                .build();
    }
}
