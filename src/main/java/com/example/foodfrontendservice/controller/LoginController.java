package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.service.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {
    private final UserIntegrationService userIntegrationService ;


    @GetMapping("/login")
    public String  autLogin(Model model) {

        return "aut/login";
    }
}
