package com.example.foodfrontendservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Получаем текущий HTTP запрос
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Передаем Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
                log.debug("🔐 Передан Authorization header в Feign Client");
            }

            // Передаем X-User-Role header
            String userRole = request.getHeader("X-User-Role");
            if (userRole != null) {
                template.header("X-User-Role", userRole);
                log.debug("👤 Передан X-User-Role: {}", userRole);
            }

            // Передаем X-User-Email header
            String userEmail = request.getHeader("X-User-Email");
            if (userEmail != null) {
                template.header("X-User-Email", userEmail);
                log.debug("📧 Передан X-User-Email: {}", userEmail);
            }

            log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}",
                    template.url(),
                    authHeader != null ? "Present" : "Missing",
                    userRole != null ? userRole : "Missing",
                    userEmail != null ? userEmail : "Missing");
        } else {
            log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
        }
    }
}