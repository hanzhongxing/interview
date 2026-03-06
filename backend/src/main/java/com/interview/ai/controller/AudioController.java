package com.interview.ai.controller;

import com.interview.ai.service.audio.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@Slf4j
public class AudioController {

    private final SpeechService speechService;

    @PostMapping("/speech")
    public ResponseEntity<StreamingResponseBody> generateSpeech(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InputStream inputStream = speechService.generate(text,SpeechService.freetts);

            StreamingResponseBody responseBody = outputStream -> {
                byte[] buffer = new byte[8192];
                int bytesRead;
                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }
            };

            HttpHeaders headers = new HttpHeaders();
            // User requested PCM, but technically it's raw audio data.
            // We'll set it to octet-stream and the frontend will handle it as PCM.
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);
        } catch (Exception e) {
            log.error("Failed to generate speech stream", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<Map<String, String>> transcribe(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("voice_", ".webm");
            file.transferTo(tempFile);

            String text = speechService.transcribe(tempFile.toRealPath().toString());

            Files.deleteIfExists(tempFile);
            return ResponseEntity.ok(Map.of("text", text));
        } catch (IOException e) {
            log.error("Failed to transcribe audio", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
