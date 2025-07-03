
// 🔥 ТАКЖЕ СОЗДАЙТЕ ЭТОТ ОТЛАДОЧНЫЙ ФИЛЬТР

package com.example.foodfrontendservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class MultipartDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String contentType = httpRequest.getContentType();

            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
                log.info("🔍 MULTIPART REQUEST DEBUG:");
                log.info("   Content-Type: {}", contentType);
                log.info("   Content-Length: {}", httpRequest.getContentLength());
                log.info("   Method: {}", httpRequest.getMethod());
                log.info("   URI: {}", httpRequest.getRequestURI());

                // Логируем параметры (только имена для безопасности)
                if (httpRequest.getParameterNames() != null) {
                    log.info("   Parameters: {}", String.join(", ",
                            java.util.Collections.list(httpRequest.getParameterNames())));
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("FileCountLimitExceededException")) {
                log.error("🔥 FileCountLimitExceededException все еще происходит! Проверьте настройки Tomcat.");
            }
            throw e;
        }
    }
}
