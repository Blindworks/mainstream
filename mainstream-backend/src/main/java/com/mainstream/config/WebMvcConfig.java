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

    @Value("${file.upload.route-image-dir:uploads/route-images}")
    private String routeImageUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded avatar files
        // Convert to absolute path to ensure correct resolution
        String avatarAbsolutePath = Paths.get(avatarUploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarAbsolutePath + "/");

        // Serve uploaded route image files
        String routeImageAbsolutePath = Paths.get(routeImageUploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/route-images/**")
                .addResourceLocations("file:" + routeImageAbsolutePath + "/");
    }
}
