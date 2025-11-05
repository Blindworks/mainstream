package com.mainstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the unified MainStream backend service.
 * This consolidates user management, run tracking, and competition functionality
 * into a single Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class MainStreamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainStreamBackendApplication.class, args);
    }
}