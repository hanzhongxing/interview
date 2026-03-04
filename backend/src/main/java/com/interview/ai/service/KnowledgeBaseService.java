package com.interview.ai.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KnowledgeBaseService {

    @Value("${interview.data-path:/opt/web/interview/data}")
    private String dataPath;

    private String kbPath;

    @PostConstruct
    public void init() {
        this.kbPath = dataPath + "/kb";
        File dir = new File(kbPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<String> listFiles() {
        try {
            return Files.list(Paths.get(kbPath))
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list KB files", e);
            return new ArrayList<>();
        }
    }

    public void uploadFile(MultipartFile file) throws IOException {
        Path path = Paths.get(kbPath, file.getOriginalFilename());
        Files.copy(file.getInputStream(), path);
    }

    public void deleteFile(String fileName) throws IOException {
        Path path = Paths.get(kbPath, fileName);
        Files.deleteIfExists(path);
    }

    public String getKbPath() {
        return kbPath;
    }
}
