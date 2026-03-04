package com.interview.ai.controller;

import com.interview.ai.service.KnowledgeBaseService;
import com.interview.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
@CrossOrigin
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;
    private final RagService ragService;

    @GetMapping("/files")
    public List<String> listFiles() {
        return kbService.listFiles();
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        kbService.uploadFile(file);
        // Trigger re-indexing
        ragService.loadKnowledgeBase();
    }

    @DeleteMapping("/files/{fileName}")
    public void deleteFile(@PathVariable String fileName) throws IOException {
        kbService.deleteFile(fileName);
        // Note: Full re-indexing might be needed if using InMemoryStore without
        // individual delete support
        ragService.loadKnowledgeBase();
    }
}
