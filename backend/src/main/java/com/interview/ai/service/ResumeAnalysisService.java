package com.interview.ai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ChatLanguageModel chatModel;

    public String analyzeResume(Path resumePath, String jobDescription) {
        Document document = FileSystemDocumentLoader.loadDocument(resumePath, new ApacheTikaDocumentParser());
        String resumeText = document.text();

        String prompt = String.format(
                "请分析以下简历与岗位需求的匹配度。\n\n" +
                        "岗位需求：\n%s\n\n" +
                        "简历内容：\n%s\n\n" +
                        "请提供以下格式的分析报告：\n" +
                        "1. 核心优势\n" +
                        "2. 潜在不足\n" +
                        "3. 建议面试提问点",
                jobDescription, resumeText);

        return chatModel.generate(prompt);
    }

    public com.interview.ai.model.Job matchBestJob(Path resumePath, java.util.List<com.interview.ai.model.Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return null;
        }

        Document document = FileSystemDocumentLoader.loadDocument(resumePath, new ApacheTikaDocumentParser());
        String resumeText = document.text();

        StringBuilder jobsInfo = new StringBuilder();
        for (int i = 0; i < jobs.size(); i++) {
            jobsInfo.append(String.format("ID: %s\nTitle: %s\nDescription: %s\n---\n",
                    jobs.get(i).getId(), jobs.get(i).getTitle(), jobs.get(i).getDescription()));
        }

        String prompt = String.format(
                "以下是多个岗位需求：\n%s\n\n" +
                        "候选人简历内容：\n%s\n\n" +
                        "请根据简历内容，挑选出最匹配的一个岗位，只返回该岗位的ID。不要返回任何其他文字。",
                jobsInfo.toString(), resumeText);

        String bestJobId = chatModel.generate(prompt).trim();

        return jobs.stream()
                .filter(j -> j.getId().equals(bestJobId) || bestJobId.contains(j.getId()))
                .findFirst()
                .orElse(jobs.get(0)); // Default to first if matching fails
    }
}
