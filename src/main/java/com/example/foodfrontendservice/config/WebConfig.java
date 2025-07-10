package com.example.foodfrontendservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;           // для @CurrentUser Long userId
    private final CurrentUserInfoArgumentResolver currentUserInfoArgumentResolver;   // для @CurrentUser UserTokenInfo userInfo

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("jwtAuthenticationFilter");
        return registration;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // ✅ Добавляем оба ArgumentResolver для гибкости
        resolvers.add(currentUserArgumentResolver);        // обрабатывает @CurrentUser Long userId
        resolvers.add(currentUserInfoArgumentResolver);    // обрабатывает @CurrentUser UserTokenInfo userInfo
    }
}