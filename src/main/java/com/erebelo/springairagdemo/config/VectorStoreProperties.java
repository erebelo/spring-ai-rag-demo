package com.erebelo.springairagdemo.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag.vectorstore.milvus")
public class VectorStoreProperties {

    private List<Resource> documentsToLoad;

}
