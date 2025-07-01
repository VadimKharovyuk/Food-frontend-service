package com.example.foodfrontendservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {

    /**
     * ✅ ЕДИНСТВЕННЫЙ Encoder для поддержки multipart/form-data
     */
    @Bean
    public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        log.info("🔧 Настройка SpringFormEncoder для поддержки multipart в Feign");
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    /**
     * ✅ Interceptor для передачи заголовков аутентификации
     */
    @Bean
    public RequestInterceptor authInterceptor() {
        return new FeignAuthInterceptor();
    }

    /**
     * ✅ Внутренний класс для обработки аутентификации
     */
    @Slf4j
    public static class FeignAuthInterceptor implements RequestInterceptor {

        // Константы для заголовков
        private static final String AUTHORIZATION_HEADER = "Authorization";
        private static final String USER_ROLE_HEADER = "X-User-Role";
        private static final String USER_EMAIL_HEADER = "X-User-Email";
        private static final String USER_ID_HEADER = "X-User-Id";
        private static final String CONTENT_TYPE_HEADER = "Content-Type";

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

                    // ✅ НЕ переопределяем Content-Type для multipart запросов
                    if (!isMultipartRequest(template)) {
                        template.header("Accept", "application/json");
                    }

                    // Логируем информацию о запросе
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
         * Проверяет, является ли запрос multipart
         */
        private boolean isMultipartRequest(RequestTemplate template) {
            return template.headers().containsKey(CONTENT_TYPE_HEADER) &&
                    template.headers().get(CONTENT_TYPE_HEADER).stream()
                            .anyMatch(value -> value.contains("multipart/form-data"));
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
}