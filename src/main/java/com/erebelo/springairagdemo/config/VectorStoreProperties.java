package com.erebelo.springairagdemo.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "rag.vectorstore")
public class VectorStoreProperties {

    private List<Resource> resources;

}
