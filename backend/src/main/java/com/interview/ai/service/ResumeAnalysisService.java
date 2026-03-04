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
                        "1. 匹配得分 (0-100)\n" +
                        "2. 核心优势\n" +
                        "3. 潜在不足\n" +
                        "4. 建议面试提问点",
                jobDescription, resumeText);

        return chatModel.generate(prompt);
    }
}
