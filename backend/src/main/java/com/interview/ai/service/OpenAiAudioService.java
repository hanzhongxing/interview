package com.interview.ai.service;

import com.interview.ai.model.LlmConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiAudioService {

    private final LlmConfigService llmConfigService;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public byte[] generateSpeech(String text) {
        LlmConfig config = llmConfigService.getActiveConfig(LlmConfig.ModelType.SPEECH)
                .orElseThrow(() -> new RuntimeException("TTS Model not configured"));

        String json = String.format("{\"model\": \"%s\", \"input\": \"%s\", \"voice\": \"alloy\"}",
                config.getModelName(), text.replace("\"", "\\\""));

        Request request = new Request.Builder()
                .url(config.getBaseUrl())
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("TTS Request failed: " + response.body().string());
            }
            return response.body().bytes();
        } catch (IOException e) {
            log.error("Failed to generate speech", e);
            throw new RuntimeException("TTS service error", e);
        }
    }

    public String transcribe(Path audioPath) {
        LlmConfig config = llmConfigService.getActiveConfig(LlmConfig.ModelType.TRANSCRIPTION)
                .orElseThrow(() -> new RuntimeException("Transcription Model not configured"));

        RequestBody fileBody = RequestBody.create(audioPath.toFile(), MediaType.parse("audio/webm"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", config.getModelName())
                .addFormDataPart("file", audioPath.getFileName().toString(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(config.getBaseUrl())
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Transcription Request failed: " + response.body().string());
            }
            String responseBody = response.body().string();
            return objectMapper.readTree(responseBody).get("text").asText();
        } catch (IOException e) {
            log.error("Failed to transcribe audio", e);
            throw new RuntimeException("Transcription service error", e);
        }
    }
}
