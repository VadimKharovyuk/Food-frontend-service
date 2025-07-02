package com.example.foodfrontendservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Component
@Order(1) // Выполняется первым
@Slf4j
@ConditionalOnProperty(
        name = "app.debug.headers.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class HeaderDebugFilter implements Filter {

    // Список URL паттернов для мониторинга
    private static final List<String> DEBUG_ENDPOINTS = Arrays.asList(
            "/api/frontend/stores",
            "/api/frontend/products",
            "/api/frontend/orders",
            "/api/frontend/users"
    );

    // Чувствительные заголовки (скрываем значения)
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "authorization", "password", "secret", "token", "key"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String uri = httpRequest.getRequestURI();

            // Проверяем, нужно ли логировать этот эндпоинт
            if (shouldLogEndpoint(uri)) {
                logRequestHeaders(httpRequest);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Проверяет, нужно ли логировать заголовки для данного эндпоинта
     */
    private boolean shouldLogEndpoint(String uri) {
        return DEBUG_ENDPOINTS.stream().anyMatch(uri::contains);
    }

    /**
     * Логирует заголовки запроса
     */
    private void logRequestHeaders(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        log.info("🔍 HeaderDebugFilter: {} {}", method, uri);

        // Логируем все заголовки
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = getHeaderValue(request, headerName);
            log.debug("📋 Header: {} = {}", headerName, headerValue);
        }

        // Специально проверяем важные заголовки
        log.info("🔐 Authorization: {}", getHeaderValue(request, "Authorization"));
        log.info("🆔 X-User-Id: {}", request.getHeader("X-User-Id"));
        log.info("👤 X-User-Role: {}", request.getHeader("X-User-Role"));
        log.info("📧 X-User-Email: {}", request.getHeader("X-User-Email"));
        log.info("🌐 X-Forwarded-For: {}", request.getHeader("X-Forwarded-For"));
        log.info("🏠 X-Real-IP: {}", request.getHeader("X-Real-IP"));
    }

    /**
     * Получает значение заголовка с маскировкой чувствительных данных
     */
    private String getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);

        if (value == null) {
            return "null";
        }

        // Маскируем чувствительные заголовки
        if (isSensitiveHeader(headerName)) {
            return value.length() > 10 ?
                    value.substring(0, 10) + "..." + " (masked)" :
                    "***masked***";
        }

        return value;
    }

    /**
     * Проверяет, является ли заголовок чувствительным
     */
    private boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.stream()
                .anyMatch(sensitive -> headerName.toLowerCase().contains(sensitive));
    }
}