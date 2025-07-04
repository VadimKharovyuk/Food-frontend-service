package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.service.UserIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {



    @GetMapping("/login")
    public String autLogin(Model model) {
        log.info("📄 GET /login - Показываем страницу авторизации");
        return "aut/login";
    }


}
