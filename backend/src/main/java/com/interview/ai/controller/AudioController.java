package com.interview.ai.controller;

import com.interview.ai.service.OpenAiAudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@Slf4j
public class AudioController {

    private final OpenAiAudioService audioService;

    @PostMapping("/speech")
    public ResponseEntity<byte[]> generateSpeech(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] audioData = audioService.generateSpeech(text);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentLength(audioData.length);
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to generate speech", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<Map<String, String>> transcribe(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("voice_", ".webm");
            file.transferTo(tempFile);

            String text = audioService.transcribe(java.util.Objects.requireNonNull(tempFile));

            Files.deleteIfExists(tempFile);
            return ResponseEntity.ok(Map.of("text", text));
        } catch (IOException e) {
            log.error("Failed to transcribe audio", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
