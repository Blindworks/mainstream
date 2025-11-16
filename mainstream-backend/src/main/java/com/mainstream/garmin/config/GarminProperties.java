package com.mainstream.garmin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mainstream.garmin")
@Data
public class GarminProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String apiUrl;
    private String authUrl;
    private String tokenUrl;
}
