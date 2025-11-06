package com.mainstream.nike.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mainstream.nike")
@Data
public class NikeProperties {
    private String apiUrl = "https://api.nike.com";
}
