package com.biblioteca.api.infra;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v2/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5500")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-API-Key", "X-Idempotency-Key", "X-API-Version", "Accept")
                .exposedHeaders("Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset", "X-Idempotency-Replayed")
                .allowCredentials(false).maxAge(3600);
        registry.addMapping("/api/chaves-api")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5500")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-API-Key", "X-Idempotency-Key", "X-API-Version", "Accept")
                .exposedHeaders("Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset", "X-Idempotency-Replayed")
                .allowCredentials(false).maxAge(3600);
    }
}
