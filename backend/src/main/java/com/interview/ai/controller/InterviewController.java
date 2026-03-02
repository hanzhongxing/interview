package com.interview.ai.controller;

import com.interview.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewController {

    private final RagService ragService;

    @Value("${interview.resume-path}")
    private String resumePath;

    @PostMapping("/upload-resume")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(resumePath, fileName);
            Files.copy(file.getInputStream(), path);

            // Ingest into RAG
            ragService.ingestResume(path);

            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            log.error("Failed to upload resume", e);
            return ResponseEntity.internalServerError().body("Failed to upload resume: " + e.getMessage());
        }
    }
}
