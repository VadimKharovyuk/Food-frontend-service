package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.Client.ProductServiceClient;
import com.example.foodfrontendservice.dto.CreateProductDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.SingleProductResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new CreateProductDto());
        return "products/create";
    }

        @PostMapping("/create")
        public String createProduct(@Valid @ModelAttribute CreateProductDto product,
                                    BindingResult bindingResult,
                                    @RequestParam("imageFile") MultipartFile imageFile,
                                    @RequestParam(value = "userId", defaultValue = "1") Long userId,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

            log.info("🚀 Creating product: {}", product.getName());

            try {
                // Простая валидация
                if (bindingResult.hasErrors()) {
                    log.warn("Form validation errors: {}", bindingResult.getAllErrors());
                    model.addAttribute("error", "Ошибки в форме");
                    model.addAttribute("product", product);
                    return "products/create";
                }

                if (imageFile == null || imageFile.isEmpty()) {
                    model.addAttribute("error", "Выберите изображение");
                    model.addAttribute("product", product);
                    return "products/create";
                }

                if (imageFile.getSize() > 10 * 1024 * 1024) {
                    model.addAttribute("error", "Файл слишком большой (макс. 10MB)");
                    model.addAttribute("product", product);
                    return "products/create";
                }

                // Проверка типа файла
                if (!isValidContentType(imageFile.getContentType())) {
                    model.addAttribute("error", "Неправильный тип файла. Только JPG, PNG, GIF, WEBP");
                    model.addAttribute("product", product);
                    return "products/create";
                }

                // 🔥 КОНВЕРТИРУЕМ ПРОДУКТ В JSON СТРОКУ
                String productJson = objectMapper.writeValueAsString(product);
                log.info("📤 Sending product JSON: {}", productJson);
                log.info("📤 Sending image: {} ({} bytes)", imageFile.getOriginalFilename(), imageFile.getSize());

                // Отправляем в Product Service
                ResponseEntity<SingleProductResponseWrapper> response =
                        productServiceClient.createProduct(productJson, imageFile, userId);

                log.info("✅ Response status: {}", response.getStatusCode());

                if (response.getStatusCode().is2xxSuccessful()) {
                    SingleProductResponseWrapper result = response.getBody();
                    if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
                        log.info("🎉 Product created successfully!");
                        redirectAttributes.addFlashAttribute("success", "Продукт создан!");
                        return "redirect:/products";
                    } else {
                        String errorMsg = result != null ? result.getMessage() : "Неизвестная ошибка";
                        log.warn("❌ Product creation failed: {}", errorMsg);
                        model.addAttribute("error", "Ошибка создания: " + errorMsg);
                        model.addAttribute("product", product);
                        return "products/create";
                    }
                } else {
                    log.warn("❌ Bad response status: {}", response.getStatusCode());
                    model.addAttribute("error", "Ошибка сервера: " + response.getStatusCode());
                    model.addAttribute("product", product);
                    return "products/create";
                }

            } catch (Exception e) {
                log.error("💥 Error creating product", e);
                model.addAttribute("error", "Произошла ошибка: " + e.getMessage());
                model.addAttribute("product", product);
                return "products/create";
            }
        }

        private boolean isValidContentType(String contentType) {
            if (contentType == null) {
                return false;
            }

            String normalizedType = contentType.toLowerCase().trim();

            return normalizedType.equals("image/jpeg") ||
                    normalizedType.equals("image/png") ||
                    normalizedType.equals("image/gif") ||
                    normalizedType.equals("image/webp");
        }
    }
