package com.erebelo.springairagdemo.bootstrap;

import com.erebelo.springairagdemo.config.VectorStoreProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class LoadVectorStore implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final VectorStoreProperties vectorStoreProperties;

    @Override
    public void run(String... args) {
        if (vectorStore.similaritySearch("NBA").isEmpty()) {
            log.info("Loading documents into vector store");

            vectorStoreProperties.getResources().forEach(resource -> {
                log.info("Loading resource: {}", resource.getFilename());
                List<Document> documents = new TikaDocumentReader(resource).get();
                List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
                vectorStore.add(splitDocuments);
            });
        }

        log.info("Vector store loaded");
    }
}
