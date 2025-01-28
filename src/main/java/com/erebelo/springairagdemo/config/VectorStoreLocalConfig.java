package com.erebelo.springairagdemo.config;

import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class VectorStoreLocalConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel,
            VectorStorePropertiesConfig vectorStoreProperties) {
        File vectorStoreFile = new File(vectorStoreProperties.getPath());
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        createDirectoryIfNotExists(vectorStoreFile.getParentFile());

        if (!vectorStoreFile.exists() || vectorStoreProperties.isRebuild()) {
            return buildVectorStore(vectorStoreProperties.getResources(), vectorStoreFile, vectorStore);
        }

        log.info("Loading existing vector store from file: {}", vectorStoreFile.getName());
        try {
            vectorStore.load(vectorStoreFile);
            log.info("Vector store loaded successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error loading vector store from file: " + vectorStoreFile.getName(), e);
        }

        return vectorStore;
    }

    private void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();
            if (dirsCreated) {
                log.info("Directory created: {}", directory.getAbsolutePath());
            } else {
                log.warn("Failed to create directory: {}", directory.getAbsolutePath());
            }
        }
    }

    private SimpleVectorStore buildVectorStore(List<Resource> resources, File vectorStoreFile,
            SimpleVectorStore vectorStore) {
        if (vectorStoreFile.exists()) {
            log.info("Deleting existing vector store file: {}", vectorStoreFile.getAbsolutePath());
            if (!vectorStoreFile.delete()) {
                throw new RuntimeException(
                        "Failed to delete existing vector store file: " + vectorStoreFile.getAbsolutePath());
            }
        }

        log.info("Loading {} resources into vector store...", resources.size());
        resources.forEach(resource -> {
            log.info("Loading resource: {}", resource.getFilename());
            try {
                List<Document> documents = new TikaDocumentReader(resource).get();
                List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
                vectorStore.add(splitDocuments);
            } catch (Exception e) {
                log.error("Error processing resource: {}", resource.getFilename(), e);
            }
        });

        try {
            vectorStore.save(vectorStoreFile);
            log.info("Vector store saved successfully to file: {}", vectorStoreFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Error saving vector store", e);
        }

        return vectorStore;
    }
}
