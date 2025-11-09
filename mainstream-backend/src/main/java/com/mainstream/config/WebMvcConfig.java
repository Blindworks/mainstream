package com.mainstream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded avatar files
        // Convert to absolute path to ensure correct resolution
        String absolutePath = Paths.get(avatarUploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
