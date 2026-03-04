package com.interview.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.ai.model.Job;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final ObjectMapper objectMapper;

    @Value("${interview.data-path:/opt/web/interview/data}")
    private String dataPath;

    private String jobFilePath;

    @PostConstruct
    public void init() {
        this.jobFilePath = dataPath + "/jobs.json";
        File file = new File(jobFilePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                objectMapper.writeValue(file, new ArrayList<Job>());
            } catch (IOException e) {
                log.error("Failed to create jobs.json", e);
            }
        }
    }

    public List<Job> getAllJobs() {
        try {
            return objectMapper.readValue(new File(jobFilePath), new TypeReference<List<Job>>() {
            });
        } catch (IOException e) {
            log.error("Failed to read jobs", e);
            return new ArrayList<>();
        }
    }

    public void saveJob(Job job) {
        List<Job> jobs = getAllJobs();
        jobs.removeIf(j -> j.getId().equals(job.getId()));
        jobs.add(job);
        saveAll(jobs);
    }

    public void deleteJob(String id) {
        List<Job> jobs = getAllJobs();
        jobs.removeIf(j -> j.getId().equals(id));
        saveAll(jobs);
    }

    private void saveAll(List<Job> jobs) {
        try {
            objectMapper.writeValue(new File(jobFilePath), jobs);
        } catch (IOException e) {
            log.error("Failed to save jobs", e);
        }
    }
}
