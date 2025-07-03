package com.example.foodfrontendservice.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 🔥 УВЕЛИЧИВАЕМ лимиты
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));  // Увеличили
        factory.setFileSizeThreshold(DataSize.ofKilobytes(0));  // Изменили на 0

        return factory.createMultipartConfig();
    }
}