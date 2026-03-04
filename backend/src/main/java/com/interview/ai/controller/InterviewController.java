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
import java.util.Map;
import java.util.UUID;
import com.interview.ai.service.ResumeAnalysisService;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewController {

    private final RagService ragService;
    private final ResumeAnalysisService analysisService;
    private final com.interview.ai.service.JobService jobService;

    @Value("${interview.resume-path}")
    private String resumePath;

    @PostMapping("/upload-resume")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(resumePath, fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path);

            // Ingest into RAG
            ragService.ingestResume(path);

            // Match Job
            java.util.List<com.interview.ai.model.Job> allJobs = jobService.getAllJobs();
            com.interview.ai.model.Job matchedJob = analysisService.matchBestJob(path, allJobs);

            String jd = matchedJob != null ? matchedJob.getDescription() : "默认通用开发岗位需求";
            String jobTitle = matchedJob != null ? matchedJob.getTitle() : "通用岗位";

            // Perform Analysis
            String analysisResults = analysisService.analyzeResume(path, jd);

            return ResponseEntity.ok(Map.of(
                    "sessionId", UUID.randomUUID().toString(),
                    "fileName", fileName,
                    "matchedJob", jobTitle,
                    "analysis", analysisResults));
        } catch (IOException e) {
            log.error("Failed to upload resume", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload resume: " + e.getMessage()));
        }
    }
}
