package com.erebelo.springairagdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "rag.vectorstore")
public class VectorStoreLocalProperties extends VectorStoreProperties {

    private String path;
    private boolean rebuild;

}
