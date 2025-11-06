package com.mainstream.strava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mainstream.strava")
@Data
public class StravaProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String apiUrl;
    private String authUrl;
    private String tokenUrl;
}
