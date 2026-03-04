package com.interview.ai.controller;

import com.interview.ai.service.RagService;
import com.interview.ai.service.ResumeAnalysisService;
import com.interview.ai.service.JobService;
import com.interview.ai.model.Job;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewController {

    private final RagService ragService;
    private final ResumeAnalysisService analysisService;
    private final JobService jobService;

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
            log.info("ingestResume: {}", fileName);

            // Ingest into RAG
            ragService.ingestResume(path);

            // Match Jobs
            List<Job> allJobs = jobService.getAllJobs();
            List<Job> matchedJobs = analysisService.matchJobs(path, allJobs);

            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("sessionId", UUID.randomUUID().toString());

            if (matchedJobs.isEmpty()) {
                response.put("matchStatus", "NONE");
            } else if (matchedJobs.size() == 1) {
                response.put("matchStatus", "SINGLE");
                Job matchedJob = matchedJobs.get(0);
                response.put("matchedJob", matchedJob.getTitle());
                response.put("jobId", matchedJob.getId());

                // Perform Analysis for single match
                String analysisResults = analysisService.analyzeResume(path, matchedJob.getDescription());
                response.put("analysis", analysisResults);
            } else {
                response.put("matchStatus", "MULTIPLE");
                response.put("matches", matchedJobs);
            }

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload resume", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload resume: " + e.getMessage()));
        }
    }

    @PostMapping("/select-job")
    public ResponseEntity<Map<String, Object>> selectJob(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        String jobId = request.get("jobId");

        if (fileName == null || jobId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fileName or jobId"));
        }

        Path path = Paths.get(resumePath, fileName);
        if (!Files.exists(path)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Resume file not found"));
        }

        Job job = jobService.getAllJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElse(null);

        if (job == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job not found"));
        }

        String analysisResults = analysisService.analyzeResume(path, job.getDescription());

        return ResponseEntity.ok(Map.of(
                "analysis", analysisResults,
                "matchedJob", job.getTitle(),
                "jobId", job.getId()));
    }
}
