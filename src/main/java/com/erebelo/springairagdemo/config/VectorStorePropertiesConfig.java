package com.erebelo.springairagdemo.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag.vectorstore")
public class VectorStorePropertiesConfig {

    private String path;
    private boolean rebuild;
    private List<Resource> resources;

}
