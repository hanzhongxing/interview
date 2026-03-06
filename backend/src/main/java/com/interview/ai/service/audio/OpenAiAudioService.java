package com.interview.ai.service.audio;

import com.interview.ai.model.LlmConfig;
import com.interview.ai.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiAudioService extends BaseAudioService{

    private final LlmConfigService llmConfigService;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public InputStream generate(String text) {
        LlmConfig config = llmConfigService.getActiveConfig(LlmConfig.ModelType.SPEECH)
                .orElseThrow(() -> new RuntimeException("TTS Model not configured"));

        // Using user's requested format: PCM, voice: tongtong, and cleaning up text
        String cleanedText = text.replace("\"", "\\\"").replace("\n", "");

        String json = String.format(
                "{\"model\": \"%s\", \"input\": \"%s\", \"voice\": \"tongtong\", \"response_format\": \"pcm\"}",
                config.getModelName(), cleanedText);

        log.info("TTS streaming request json: {}", json);

        Request request = new Request.Builder()
                .url(config.getBaseUrl()) // User manually edited this to be the full URL or direct path
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("TTS Request failed: {}", errorBody);
                throw new IOException("TTS Request failed with code " + response.code());
            }
            return response.body().byteStream();
        } catch (IOException e) {
            log.error("Failed to generate speech stream", e);
            throw new RuntimeException("TTS streaming service error", e);
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
                .url(config.getBaseUrl()) // User manually edited this to be the full URL
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Transcription Request failed: {}", errorBody);
                throw new IOException("Transcription Request failed with code " + response.code());
            }
            String responseBody = response.body().string();
            return objectMapper.readTree(responseBody).get("text").asText();
        } catch (IOException e) {
            log.error("Failed to transcribe audio", e);
            throw new RuntimeException("Transcription service error", e);
        }
    }
}
