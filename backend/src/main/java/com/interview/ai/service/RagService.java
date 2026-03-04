package com.interview.ai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RagService {

    @Value("${interview.kb-path}")
    private String kbPath;

    @Value("${interview.vector-store-path}")
    private String vectorStorePath;

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    private EmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        this.embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("nomic-embed-text")
                .build();

        Path path = Paths.get(vectorStorePath);
        if (path.toFile().exists()) {
            this.embeddingStore = InMemoryEmbeddingStore.fromFile(path);
            log.info("Vector store loaded from {}", vectorStorePath);
        } else {
            this.embeddingStore = new InMemoryEmbeddingStore<>();
            log.info("New vector store initialized");
        }

        // Load KB if available
        loadKnowledgeBase();
    }

    public void loadKnowledgeBase() {
        try {
            Path path = Paths.get(kbPath);
            if (path.toFile().exists()) {
                List<Document> documents = FileSystemDocumentLoader.loadDocuments(path, new ApacheTikaDocumentParser());
                if(documents!=null&&!documents.isEmpty()) {
                    EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                            .embeddingModel(embeddingModel)
                            .embeddingStore(embeddingStore)
                            .build();
                    ingestor.ingest(documents);
                }
                saveStore();
                log.info("Knowledge base loaded and indexed from {}", kbPath);
            }
        } catch (Exception e) {
            log.error("Failed to load knowledge base", e);
        }
    }

    public void ingestResume(Path resumePath) {
        Document document = FileSystemDocumentLoader.loadDocument(resumePath, new ApacheTikaDocumentParser());
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);
        saveStore();
        log.info("Resume ingested and indexed: {}", resumePath.getFileName());
    }

    private void saveStore() {
        try {
            ((InMemoryEmbeddingStore<TextSegment>) embeddingStore).serializeToFile(vectorStorePath);
        } catch (Exception e) {
            log.error("Failed to save vector store", e);
        }
    }

    public ContentRetriever getContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .build();
    }
}
