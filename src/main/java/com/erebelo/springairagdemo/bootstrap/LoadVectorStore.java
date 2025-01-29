package com.erebelo.springairagdemo.bootstrap;

import com.erebelo.springairagdemo.config.VectorStoreProperties;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("!local")
public class LoadVectorStore {

    // @Override
    // public void run(String... args) {
    // if (vectorStore.similaritySearch("Basketball").isEmpty() &&
    // vectorStore.similaritySearch("NBA").isEmpty()) {
    // log.info("Loading documents into vector store");
    //
    // vectorStoreProperties.getResources().forEach(resource -> {
    // log.info("Loading resource: {}", resource.getFilename());
    // List<Document> documents = new TikaDocumentReader(resource).get();
    // List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
    // vectorStore.add(splitDocuments);
    // });
    // }
    //
    // log.info("Vector store loaded");
    // }

    @Bean
    public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel,
            VectorStoreProperties vectorStoreProperties) {
        VectorStore vectorStore = MilvusVectorStore.builder(milvusClient, embeddingModel).collectionName("vector_store")
                .databaseName("default").indexType(IndexType.IVF_FLAT).metricType(MetricType.COSINE)
                .embeddingDimension(1536).batchingStrategy(new TokenCountBatchingStrategy()).initializeSchema(true)
                .build();

        vectorStoreProperties.getResources().forEach(resource -> {
            log.info("Loading resource: {}", resource.getFilename());

            List<Document> documents = new TikaDocumentReader(resource).get();
            List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
            vectorStore.add(splitDocuments);
        });

        log.info("Vector store loaded");
        return vectorStore;
    }

    @Bean
    public MilvusServiceClient milvusClient() {
        return new MilvusServiceClient(
                ConnectParam.newBuilder().withAuthorization("root", "milvus").withUri("localhost:19530").build());
    }
}
