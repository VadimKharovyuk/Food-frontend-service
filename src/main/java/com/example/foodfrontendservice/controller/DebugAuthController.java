package com.example.foodfrontendservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugAuthController {


    @GetMapping("/auth")
    public Map<String, Object> debugAuth(HttpServletRequest request) {
        Map<String, Object> debug = new HashMap<>();


        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        debug.put("headers", headers);

        // Параметры
        Map<String, String[]> parameters = request.getParameterMap();
        debug.put("parameters", parameters);

        // Сессия
        Map<String, Object> sessionData = new HashMap<>();
        if (request.getSession(false) != null) {
            Enumeration<String> sessionAttributes = request.getSession().getAttributeNames();
            while (sessionAttributes.hasMoreElements()) {
                String attrName = sessionAttributes.nextElement();
                Object attrValue = request.getSession().getAttribute(attrName);
                sessionData.put(attrName, attrValue != null ? attrValue.toString() : "null");
            }
        }
        debug.put("session", sessionData);

        // URL и метод
        debug.put("method", request.getMethod());
        debug.put("requestURL", request.getRequestURL().toString());
        debug.put("requestURI", request.getRequestURI());
        debug.put("remoteAddr", request.getRemoteAddr());

        log.info("🔍 Debug auth request: {}", debug);
        return debug;
    }

    /**
     * 🧪 Тест перенаправления с токеном
     */
    @PostMapping("/redirect-test")
    public Map<String, Object> testRedirect(HttpServletRequest request,
                                            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> result = new HashMap<>();

        // Проверяем токен в заголовках
        String authHeader = request.getHeader("Authorization");
        String customToken = request.getHeader("X-Auth-Token");

        result.put("authHeader", authHeader);
        result.put("customToken", customToken);
        result.put("body", body);
        result.put("success", authHeader != null || customToken != null);

        if (authHeader != null || customToken != null) {
            result.put("redirectUrl", "/dashboard");
            result.put("message", "Токен найден, можно перенаправлять");
        } else {
            result.put("redirectUrl", "/login?error=no_token");
            result.put("message", "Токен не найден");
        }

        log.info("🧪 Test redirect: {}", result);
        return result;
    }


    @GetMapping("/session")
    public Map<String, Object> sessionInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();

        if (request.getSession(false) != null) {
            info.put("sessionId", request.getSession().getId());
            info.put("creationTime", new Date(request.getSession().getCreationTime()));
            info.put("lastAccessedTime", new Date(request.getSession().getLastAccessedTime()));
            info.put("maxInactiveInterval", request.getSession().getMaxInactiveInterval());

            // Атрибуты сессии
            Map<String, Object> attributes = new HashMap<>();
            Enumeration<String> attrNames = request.getSession().getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String name = attrNames.nextElement();
                Object value = request.getSession().getAttribute(name);
                attributes.put(name, value != null ? value.getClass().getSimpleName() : "null");
            }
            info.put("attributes", attributes);

        } else {
            info.put("message", "Сессия не существует");
        }

        return info;
    }


    @PostMapping("/clear-session")
    public Map<String, Object> clearSession(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.getSession(false) != null) {
            request.getSession().invalidate();
            result.put("message", "Сессия очищена");
            result.put("success", true);
        } else {
            result.put("message", "Сессия не найдена");
            result.put("success", false);
        }

        return result;
    }
}