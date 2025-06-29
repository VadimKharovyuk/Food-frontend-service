//package com.example.foodfrontendservice.config;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//@Slf4j
//public class FeignAuthInterceptor implements RequestInterceptor {
//
//    @Override
//    public void apply(RequestTemplate template) {
//        // Получаем текущий HTTP запрос
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//        if (attributes != null) {
//            HttpServletRequest request = attributes.getRequest();
//
//            // Передаем Authorization header
//            String authHeader = request.getHeader("Authorization");
//            if (authHeader != null) {
//                template.header("Authorization", authHeader);
//                log.debug("🔐 Передан Authorization header в Feign Client");
//            }
//
//            // Передаем X-User-Role header
//            String userRole = request.getHeader("X-User-Role");
//            if (userRole != null) {
//                template.header("X-User-Role", userRole);
//                log.debug("👤 Передан X-User-Role: {}", userRole);
//            }
//
//            // Передаем X-User-Email header
//            String userEmail = request.getHeader("X-User-Email");
//            if (userEmail != null) {
//                template.header("X-User-Email", userEmail);
//                log.debug("📧 Передан X-User-Email: {}", userEmail);
//            }
//
//            log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}",
//                    template.url(),
//                    authHeader != null ? "Present" : "Missing",
//                    userRole != null ? userRole : "Missing",
//                    userEmail != null ? userEmail : "Missing");
//        } else {
//            log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
//        }
//    }
//}

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

    // Константы для заголовков
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public void apply(RequestTemplate template) {
        try {
            // Получаем текущий HTTP запрос
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Передаем все необходимые заголовки
                transferHeader(request, template, AUTHORIZATION_HEADER, "🔐");
                transferHeader(request, template, USER_ROLE_HEADER, "👤");
                transferHeader(request, template, USER_EMAIL_HEADER, "📧");
                transferHeader(request, template, USER_ID_HEADER, "🆔");

                // Логируем общую информацию о запросе
                logFeignRequest(template, request);
            } else {
                log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при передаче заголовков в Feign Client", e);
        }
    }

    /**
     * Передает заголовок из входящего запроса в Feign запрос
     */
    private void transferHeader(HttpServletRequest request, RequestTemplate template,
                                String headerName, String emoji) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.trim().isEmpty()) {
            template.header(headerName, headerValue);
            log.debug("{} Передан {}: {}", emoji, headerName,
                    AUTHORIZATION_HEADER.equals(headerName) ? "Present" : headerValue);
        }
    }

    /**
     * Логирует информацию о Feign запросе
     */
    private void logFeignRequest(RequestTemplate template, HttpServletRequest request) {
        log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}, X-User-Id={}",
                template.url(),
                hasHeader(request, AUTHORIZATION_HEADER) ? "Present" : "Missing",
                getHeaderOrDefault(request, USER_ROLE_HEADER, "Missing"),
                getHeaderOrDefault(request, USER_EMAIL_HEADER, "Missing"),
                getHeaderOrDefault(request, USER_ID_HEADER, "Missing"));
    }

    /**
     * Проверяет наличие заголовка
     */
    private boolean hasHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Получает значение заголовка или значение по умолчанию
     */
    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        String value = request.getHeader(headerName);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
}