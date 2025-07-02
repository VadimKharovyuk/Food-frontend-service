//////package com.example.foodfrontendservice.config;
//////
//////import feign.RequestInterceptor;
//////import feign.RequestTemplate;
//////import jakarta.servlet.http.HttpServletRequest;
//////import lombok.extern.slf4j.Slf4j;
//////import org.springframework.stereotype.Component;
//////import org.springframework.web.context.request.RequestContextHolder;
//////import org.springframework.web.context.request.ServletRequestAttributes;
//////
//////@Slf4j
//////public class FeignAuthInterceptor implements RequestInterceptor {
//////
//////    @Override
//////    public void apply(RequestTemplate template) {
//////        // Получаем текущий HTTP запрос
//////        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//////
//////        if (attributes != null) {
//////            HttpServletRequest request = attributes.getRequest();
//////
//////            // Передаем Authorization header
//////            String authHeader = request.getHeader("Authorization");
//////            if (authHeader != null) {
//////                template.header("Authorization", authHeader);
//////                log.debug("🔐 Передан Authorization header в Feign Client");
//////            }
//////
//////            // Передаем X-User-Role header
//////            String userRole = request.getHeader("X-User-Role");
//////            if (userRole != null) {
//////                template.header("X-User-Role", userRole);
//////                log.debug("👤 Передан X-User-Role: {}", userRole);
//////            }
//////
//////            // Передаем X-User-Email header
//////            String userEmail = request.getHeader("X-User-Email");
//////            if (userEmail != null) {
//////                template.header("X-User-Email", userEmail);
//////                log.debug("📧 Передан X-User-Email: {}", userEmail);
//////            }
//////
//////            log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}",
//////                    template.url(),
//////                    authHeader != null ? "Present" : "Missing",
//////                    userRole != null ? userRole : "Missing",
//////                    userEmail != null ? userEmail : "Missing");
//////        } else {
//////            log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
//////        }
//////    }
//////}
////
////package com.example.foodfrontendservice.config;
////import feign.RequestInterceptor;
////import feign.RequestTemplate;
////import feign.codec.Encoder;
////import feign.form.spring.SpringFormEncoder;
////import org.springframework.beans.factory.ObjectFactory;
////import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
////import org.springframework.cloud.openfeign.support.SpringEncoder;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import lombok.extern.slf4j.Slf4j;
////import feign.RequestInterceptor;
////import feign.RequestTemplate;
////import feign.form.spring.SpringFormEncoder;
////import jakarta.servlet.http.HttpServletRequest;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
////import org.springframework.cloud.openfeign.support.SpringEncoder;
////import org.springframework.context.annotation.Bean;
////import org.springframework.web.context.request.RequestContextHolder;
////import org.springframework.web.context.request.ServletRequestAttributes;
////
////@Slf4j
////
////public class FeignAuthInterceptor implements RequestInterceptor {
////
////    // Константы для заголовков
////    private static final String AUTHORIZATION_HEADER = "Authorization";
////    private static final String USER_ROLE_HEADER = "X-User-Role";
////    private static final String USER_EMAIL_HEADER = "X-User-Email";
////    private static final String USER_ID_HEADER = "X-User-Id";
////
////    @Override
////    public void apply(RequestTemplate template) {
////        try {
////            // Получаем текущий HTTP запрос
////            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
////
////            if (attributes != null) {
////                HttpServletRequest request = attributes.getRequest();
////
////                // Передаем все необходимые заголовки
////                transferHeader(request, template, AUTHORIZATION_HEADER, "🔐");
////                transferHeader(request, template, USER_ROLE_HEADER, "👤");
////                transferHeader(request, template, USER_EMAIL_HEADER, "📧");
////                transferHeader(request, template, USER_ID_HEADER, "🆔");
////
////                // Логируем общую информацию о запросе
////                logFeignRequest(template, request);
////            } else {
////                log.warn("⚠️ Нет доступного RequestContext для передачи заголовков в Feign Client");
////            }
////        } catch (Exception e) {
////            log.error("❌ Ошибка при передаче заголовков в Feign Client", e);
////        }
////    }
////
////    /**
////     * Передает заголовок из входящего запроса в Feign запрос
////     */
////    private void transferHeader(HttpServletRequest request, RequestTemplate template,
////                                String headerName, String emoji) {
////        String headerValue = request.getHeader(headerName);
////        if (headerValue != null && !headerValue.trim().isEmpty()) {
////            template.header(headerName, headerValue);
////            log.debug("{} Передан {}: {}", emoji, headerName,
////                    AUTHORIZATION_HEADER.equals(headerName) ? "Present" : headerValue);
////        }
////    }
////
////    /**
////     * Логирует информацию о Feign запросе
////     */
////    private void logFeignRequest(RequestTemplate template, HttpServletRequest request) {
////        log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}, X-User-Id={}",
////                template.url(),
////                hasHeader(request, AUTHORIZATION_HEADER) ? "Present" : "Missing",
////                getHeaderOrDefault(request, USER_ROLE_HEADER, "Missing"),
////                getHeaderOrDefault(request, USER_EMAIL_HEADER, "Missing"),
////                getHeaderOrDefault(request, USER_ID_HEADER, "Missing"));
////    }
////
////    /**
////     * Проверяет наличие заголовка
////     */
////    private boolean hasHeader(HttpServletRequest request, String headerName) {
////        String value = request.getHeader(headerName);
////        return value != null && !value.trim().isEmpty();
////    }
////
////    /**
////     * Получает значение заголовка или значение по умолчанию
////     */
////    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
////        String value = request.getHeader(headerName);
////        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
////    }
////
////    @Bean
////    public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
////        return new SpringFormEncoder(new SpringEncoder(messageConverters));
////    }
////}
//
////
////import feign.RequestInterceptor;
////import feign.RequestTemplate;
////import jakarta.servlet.http.HttpServletRequest;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.stereotype.Component;
////import org.springframework.web.context.request.RequestContextHolder;
////import org.springframework.web.context.request.ServletRequestAttributes;
////
////import com.example.foodfrontendservice.dto.UserResponseDto;
////
////@Slf4j
////@Component
////public class FeignAuthInterceptor implements RequestInterceptor {
////
////    private static final String AUTHORIZATION_HEADER = "Authorization";
////    private static final String USER_ROLE_HEADER = "X-User-Role";
////    private static final String USER_EMAIL_HEADER = "X-User-Email";
////    private static final String USER_ID_HEADER = "X-User-Id";
////
////    @Override
////    public void apply(RequestTemplate template) {
////        try {
////            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
////
////            if (attributes != null) {
////                HttpServletRequest request = attributes.getRequest();
////
////                // Получаем токен из разных источников
////                String token = extractTokenFromRequest(request);
////
////                if (token != null) {
////                    // Добавляем Authorization header
////                    template.header(AUTHORIZATION_HEADER, "Bearer " + token);
////                    log.debug("🔐 Добавлен Authorization header в Feign запрос");
////                }
////
////                // Пытаемся получить данные пользователя из сессии
////                UserResponseDto user = (UserResponseDto) request.getSession().getAttribute("currentUser");
////                if (user != null) {
////                    template.header(USER_ROLE_HEADER, user.getUserRole().name());
////                    template.header(USER_EMAIL_HEADER, user.getEmail());
////                    template.header(USER_ID_HEADER, user.getId().toString());
////
////                    log.debug("👤 Добавлены пользовательские заголовки: роль={}, email={}",
////                            user.getUserRole(), user.getEmail());
////                }
////
////                // Также пытаемся получить из заголовков запроса
////                transferHeaderIfExists(request, template, USER_ROLE_HEADER);
////                transferHeaderIfExists(request, template, USER_EMAIL_HEADER);
////                transferHeaderIfExists(request, template, USER_ID_HEADER);
////
////                logFeignRequest(template, token, user);
////            } else {
////                log.warn("⚠️ Нет доступного RequestContext для Feign Client");
////            }
////        } catch (Exception e) {
////            log.error("❌ Ошибка в FeignAuthInterceptor", e);
////        }
////    }
////
////    private String extractTokenFromRequest(HttpServletRequest request) {
////        // 1. Authorization заголовок
////        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
////        if (authHeader != null && authHeader.startsWith("Bearer ")) {
////            return authHeader.substring(7);
////        }
////
////        // 2. Атрибут запроса (установленный контроллером)
////        String attrToken = (String) request.getAttribute(AUTHORIZATION_HEADER);
////        if (attrToken != null && attrToken.startsWith("Bearer ")) {
////            return attrToken.substring(7);
////        }
////
////        // 3. Сессия
////        String sessionToken = (String) request.getSession().getAttribute("authToken");
////        if (sessionToken != null && !sessionToken.isEmpty()) {
////            return sessionToken;
////        }
////
////        // 4. Кастомный заголовок
////        String customHeader = request.getHeader("X-Auth-Token");
////        if (customHeader != null && !customHeader.isEmpty()) {
////            return customHeader;
////        }
////
////        return null;
////    }
////
////    private void transferHeaderIfExists(HttpServletRequest request, RequestTemplate template, String headerName) {
////        String headerValue = request.getHeader(headerName);
////        if (headerValue != null && !headerValue.trim().isEmpty()) {
////            template.header(headerName, headerValue);
////        }
////    }
////
////    private void logFeignRequest(RequestTemplate template, String token, UserResponseDto user) {
////        log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}, X-User-Id={}",
////                template.url(),
////                token != null ? "Present" : "Missing",
////                user != null ? user.getUserRole().name() : "Missing",
////                user != null ? user.getEmail() : "Missing",
////                user != null ? user.getId().toString() : "Missing");
////    }
////}
//
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
//import com.example.foodfrontendservice.dto.UserResponseDto;
//
//@Slf4j
//@Component
//public class FeignAuthInterceptor implements RequestInterceptor {
//
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String USER_ROLE_HEADER = "X-User-Role";
//    private static final String USER_EMAIL_HEADER = "X-User-Email";
//    private static final String USER_ID_HEADER = "X-User-Id";
//
//    @Override
//    public void apply(RequestTemplate template) {
//        try {
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//            if (attributes != null) {
//                HttpServletRequest request = attributes.getRequest();
//
//                // Получаем токен из разных источников
//                String token = extractTokenFromRequest(request);
//
//                if (token != null) {
//                    // Добавляем Authorization header
//                    template.header(AUTHORIZATION_HEADER, "Bearer " + token);
//                    log.debug("🔐 Добавлен Authorization header в Feign запрос");
//                }
//
//                // Пытаемся получить данные пользователя из сессии
//                UserResponseDto user = (UserResponseDto) request.getSession().getAttribute("currentUser");
//                if (user != null) {
//                    template.header(USER_ROLE_HEADER, user.getUserRole().name());
//                    template.header(USER_EMAIL_HEADER, user.getEmail());
//                    template.header(USER_ID_HEADER, user.getId().toString());
//
//                    log.debug("👤 Добавлены пользовательские заголовки: роль={}, email={}",
//                            user.getUserRole(), user.getEmail());
//                }
//
//                // Также пытаемся получить из заголовков запроса
//                transferHeaderIfExists(request, template, USER_ROLE_HEADER);
//                transferHeaderIfExists(request, template, USER_EMAIL_HEADER);
//                transferHeaderIfExists(request, template, USER_ID_HEADER);
//
//                logFeignRequest(template, token, user);
//            } else {
//                log.warn("⚠️ Нет доступного RequestContext для Feign Client");
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка в FeignAuthInterceptor", e);
//        }
//    }
//
//    private String extractTokenFromRequest(HttpServletRequest request) {
//        // 1. Authorization заголовок
//        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//
//        // 2. Атрибут запроса (установленный контроллером)
//        String attrToken = (String) request.getAttribute(AUTHORIZATION_HEADER);
//        if (attrToken != null && attrToken.startsWith("Bearer ")) {
//            return attrToken.substring(7);
//        }
//
//        // 3. Сессия
//        String sessionToken = (String) request.getSession().getAttribute("authToken");
//        if (sessionToken != null && !sessionToken.isEmpty()) {
//            return sessionToken;
//        }
//
//        // 4. Кастомный заголовок
//        String customHeader = request.getHeader("X-Auth-Token");
//        if (customHeader != null && !customHeader.isEmpty()) {
//            return customHeader;
//        }
//
//        return null;
//    }
//
//    private void transferHeaderIfExists(HttpServletRequest request, RequestTemplate template, String headerName) {
//        String headerValue = request.getHeader(headerName);
//        if (headerValue != null && !headerValue.trim().isEmpty()) {
//            template.header(headerName, headerValue);
//        }
//    }
//
//    private void logFeignRequest(RequestTemplate template, String token, UserResponseDto user) {
//        log.info("📤 Feign request to: {} with headers: Authorization={}, X-User-Role={}, X-User-Email={}, X-User-Id={}",
//                template.url(),
//                token != null ? "Present" : "Missing",
//                user != null ? user.getUserRole().name() : "Missing",
//                user != null ? user.getEmail() : "Missing",
//                user != null ? user.getId().toString() : "Missing");
//    }
//}
