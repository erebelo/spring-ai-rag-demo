package com.erebelo.springairagdemo.bootstrap;

import com.erebelo.springairagdemo.config.VectorStoreProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadVectorStore implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final VectorStoreProperties vectorStoreProperties;

    @Override
    public void run(String... args) {
        if (vectorStore.similaritySearch("Basketball").isEmpty() && vectorStore.similaritySearch("NBA").isEmpty()) {
            log.info("Loading documents into vector store");

            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                log.info("Loading document: {}", document.getFilename());

                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> documents = documentReader.get();

                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(documents);

                vectorStore.add(splitDocuments);
            });
        }

        log.info("Vector store loaded");
    }
}
