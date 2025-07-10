package com.example.foodfrontendservice.config;

import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenExtractor tokenExtractor;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(UserTokenInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            log.error("❌ HttpServletRequest is null");
            throw new RuntimeException("Не удалось получить HTTP запрос");
        }

        try {
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                log.warn("⚠️ Пользователь не авторизован или токен недействителен");
                throw new RuntimeException("Пользователь не авторизован");
            }

            log.debug("✅ Успешно извлечена информация о пользователе: ID={}, Email={}",
                    userInfo.getUserId(), userInfo.getEmail());

            return userInfo;

        } catch (Exception e) {
            log.error("❌ Ошибка при извлечении информации о пользователе: {}", e.getMessage());
            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
        }
    }
}