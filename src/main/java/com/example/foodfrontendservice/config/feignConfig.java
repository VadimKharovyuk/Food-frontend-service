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
import com.example.foodfrontendservice.dto.UserResponseDto;

import static org.springframework.boot.actuate.web.exchanges.Include.AUTHORIZATION_HEADER;

@Configuration
@Slf4j
public class feignConfig {

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
    @Bean("feignRequestInterceptor")  // ИСПРАВЛЕНО: другое имя bean
    public RequestInterceptor authInterceptor() {
        return new FeignAuthInterceptor();
    }

    /**
     * 🔧 Дополнительная конфигурация для отладки
     */
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.HEADERS; // Логирует заголовки для отладки
    }

    /**
     * ✅ ОБЪЕДИНЕННЫЙ и ИСПРАВЛЕННЫЙ FeignAuthInterceptor
     */
    @Slf4j
    public static class FeignAuthInterceptor implements RequestInterceptor {

        private static final String AUTHORIZATION_HEADER = "Authorization";
        private static final String USER_ROLE_HEADER = "X-User-Role";
        private static final String USER_EMAIL_HEADER = "X-User-Email";
        private static final String USER_ID_HEADER = "X-User-Id";
        private static final String CONTENT_TYPE_HEADER = "Content-Type";

        @Override
        public void apply(RequestTemplate template) {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // 🔑 Получаем токен из разных источников с приоритетом
                    String token = extractTokenFromRequest(request);

                    if (token != null) {
                        // Добавляем Authorization header
                        template.header(AUTHORIZATION_HEADER, "Bearer " + token);
                        log.debug("🔐 Добавлен Authorization header в Feign запрос (длина токена: {})", token.length());
                    } else {
                        log.debug("⚠️ Токен не найден для Feign запроса");
                    }

                    // 👤 Получаем и передаем данные пользователя из сессии
                    addUserHeadersFromSession(request, template);

                    // Также пытаемся получить из заголовков запроса (fallback)
                    transferHeaderIfExists(request, template, USER_ROLE_HEADER);
                    transferHeaderIfExists(request, template, USER_EMAIL_HEADER);
                    transferHeaderIfExists(request, template, USER_ID_HEADER);

                    // ✅ НЕ переопределяем Content-Type для multipart запросов
                    if (!isMultipartRequest(template)) {
                        template.header("Accept", "application/json");
                    }

                    // Детальное логирование
                    logFeignRequest(template, token, request);
                } else {
                    log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
                }
            } catch (Exception e) {
                log.error("❌ Ошибка в FeignAuthInterceptor", e);
            }
        }

        /**
         * 🔍 Извлекает токен из различных источников с приоритетом
         */
        private String extractTokenFromRequest(HttpServletRequest request) {
            // 1. Приоритет: Authorization заголовок
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                log.debug("✅ Токен найден в Authorization заголовке");
                return authHeader.substring(7);
            }

            // 2. Атрибут запроса (установленный контроллером)
            Object attrToken = request.getAttribute(AUTHORIZATION_HEADER);
            if (attrToken != null) {
                String tokenStr = attrToken.toString();
                if (tokenStr.startsWith("Bearer ")) {
                    log.debug("✅ Токен найден в атрибутах запроса");
                    return tokenStr.substring(7);
                }
                log.debug("✅ Токен найден в атрибутах запроса (без Bearer)");
                return tokenStr;
            }

            // 3. Сессия (основной источник после первого запроса)
            try {
                String sessionToken = (String) request.getSession().getAttribute("authToken");
                if (sessionToken != null && !sessionToken.trim().isEmpty()) {
                    log.debug("✅ Токен найден в сессии");
                    return sessionToken;
                }
            } catch (Exception e) {
                log.debug("🚫 Ошибка получения токена из сессии: {}", e.getMessage());
            }

            // 4. Кастомный заголовок
            String customHeader = request.getHeader("X-Auth-Token");
            if (customHeader != null && !customHeader.trim().isEmpty()) {
                log.debug("✅ Токен найден в X-Auth-Token заголовке");
                return customHeader;
            }

            // 5. Параметр запроса (для отладки)
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                log.debug("✅ Токен найден в параметрах запроса");
                return tokenParam;
            }

            return null;
        }

        /**
         * 👤 Добавляет заголовки с данными пользователя из сессии
         */
        private void addUserHeadersFromSession(HttpServletRequest request, RequestTemplate template) {
            try {
                UserResponseDto user = (UserResponseDto) request.getSession().getAttribute("currentUser");

                if (user != null) {
                    template.header(USER_ROLE_HEADER, user.getUserRole().name());
                    template.header(USER_EMAIL_HEADER, user.getEmail());
                    template.header(USER_ID_HEADER, user.getId().toString());

                    log.debug("👤 Добавлены пользовательские заголовки: роль={}, email={}, id={}",
                            user.getUserRole(), user.getEmail(), user.getId());
                }
            } catch (Exception e) {
                log.debug("🚫 Ошибка добавления пользовательских заголовков: {}", e.getMessage());
            }
        }

        /**
         * Передает заголовок из входящего запроса в Feign запрос (fallback)
         */
        private void transferHeaderIfExists(HttpServletRequest request, RequestTemplate template, String headerName) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.trim().isEmpty()) {
                // Проверяем, что заголовок еще не установлен
                if (!template.headers().containsKey(headerName)) {
                    template.header(headerName, headerValue);
                    log.debug("🔄 Передан заголовок {}: {}", headerName, headerValue);
                }
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
         * 📊 Детальное логирование Feign запроса
         */
        private void logFeignRequest(RequestTemplate template, String token, HttpServletRequest request) {
            try {
                UserResponseDto user = (UserResponseDto) request.getSession().getAttribute("currentUser");

                log.info("📤 Feign → {} {} | Token={} | User={} | Headers: Auth={}, Role={}, Email={}, ID={}",
                        template.method(),
                        template.url(),
                        token != null ? "Present(" + token.substring(0, Math.min(10, token.length())) + "...)" : "Missing",
                        user != null ? user.getEmail() : "Unknown",
                        hasAuthorizationHeader(template) ? "✅" : "❌",
                        getTemplateHeader(template, USER_ROLE_HEADER, "❌"),
                        getTemplateHeader(template, USER_EMAIL_HEADER, "❌"),
                        getTemplateHeader(template, USER_ID_HEADER, "❌"));

            } catch (Exception e) {
                log.info("📤 Feign → {} {} (logging error: {})",
                        template.method(), template.url(), e.getMessage());
            }
        }

        private boolean hasAuthorizationHeader(RequestTemplate template) {
            return template.headers().containsKey(AUTHORIZATION_HEADER) &&
                    !template.headers().get(AUTHORIZATION_HEADER).isEmpty();
        }

        private String getTemplateHeader(RequestTemplate template, String headerName, String defaultValue) {
            if (template.headers().containsKey(headerName) &&
                    !template.headers().get(headerName).isEmpty()) {
                return template.headers().get(headerName).iterator().next();
            }
            return defaultValue;
        }
    }


/**
 * Проверяет наличие Authorization заголовка в шаблоне
 */
private boolean hasAuthorizationHeader(RequestTemplate template) {
    return template.headers().containsKey(AUTHORIZATION_HEADER) &&
            !template.headers().get(AUTHORIZATION_HEADER).isEmpty();
}

/**
 * Получает значение заголовка из шаблона или значение по умолчанию
 */
private String getTemplateHeader(RequestTemplate template, String headerName, String defaultValue) {
    if (template.headers().containsKey(headerName) &&
            !template.headers().get(headerName).isEmpty()) {
        return template.headers().get(headerName).iterator().next();
    }
    return defaultValue;
}

/**
 * Проверяет наличие заголовка в запросе
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


