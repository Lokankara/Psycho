package com.unconscious.collective.agile.config;

import com.unconscious.collective.agile.service.JsonAgileBoardService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.mode", havingValue = "json")
public class JsonAgileDataConfiguration {

    @Bean
    public JsonAgileBoardService jsonAgileBoardService(ResourceLoader resourceLoader) {
        return new JsonAgileBoardService(resourceLoader);
    }
}
