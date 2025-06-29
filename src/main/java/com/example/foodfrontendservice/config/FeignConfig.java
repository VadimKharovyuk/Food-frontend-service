package com.example.foodfrontendservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}