package com.mainstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the unified MainStream backend service.
 * This consolidates user management, run tracking, and competition functionality
 * into a single Spring Boot application.
 */
@SpringBootApplication
public class MainStreamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainStreamBackendApplication.class, args);
    }
}