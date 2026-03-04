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

        public java.util.List<com.interview.ai.model.Job> matchJobs(Path resumePath,
                        java.util.List<com.interview.ai.model.Job> jobs) {
                if (jobs == null || jobs.isEmpty()) {
                        return java.util.Collections.emptyList();
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
                                                "请根据简历内容，挑选出匹配度较高的一个或多个岗位：\n" +
                                                "1. 如果有匹配的岗位，请按匹配度从高到低返回岗位的ID，多个ID之间用英文逗号分隔。\n" +
                                                "2. 如果完全没有匹配的岗位，请只返回字符串 \"NONE\"。\n" +
                                                "不要返回任何其他文字。",
                                jobsInfo.toString(), resumeText);

                String response = chatModel.generate(prompt).trim();
                log.info("Job matching response: {}", response);

                if ("NONE".equalsIgnoreCase(response)) {
                        return java.util.Collections.emptyList();
                }

                String[] matchedIds = response.split(",");
                java.util.List<com.interview.ai.model.Job> matches = new java.util.ArrayList<>();
                for (String id : matchedIds) {
                        String cleanId = id.trim();
                        jobs.stream()
                                        .filter(j -> j.getId().equals(cleanId) || cleanId.contains(j.getId()))
                                        .findFirst()
                                        .ifPresent(matches::add);
                }

                return matches;
        }
}
