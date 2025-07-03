package com.example.foodfrontendservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException e,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        log.error("Multipart exception: {}", e.getMessage(), e);

        String userMessage;
        if (e.getCause() != null) {
            String causeMessage = e.getCause().getMessage();
            if (causeMessage.contains("FileCountLimitExceededException")) {
                userMessage = "Слишком много файлов в запросе. Загрузите только одно изображение.";
            } else if (causeMessage.contains("FileSizeLimitExceededException")) {
                userMessage = "Размер файла слишком большой. Максимальный размер: 10MB.";
            } else if (causeMessage.contains("SizeLimitExceededException")) {
                userMessage = "Общий размер запроса превышает лимит.";
            } else {
                userMessage = "Ошибка загрузки файла. Проверьте размер и формат изображения.";
            }
        } else {
            userMessage = "Ошибка обработки формы. Попробуйте еще раз.";
        }

        redirectAttributes.addFlashAttribute("error", userMessage);
        return "redirect:/products/create";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              RedirectAttributes redirectAttributes) {
        log.error("File size exceeded: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error",
                "Размер файла превышает максимально допустимый (10MB)");
        return "redirect:/products/create";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e,
                                              RedirectAttributes redirectAttributes) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("multipart")) {
            log.error("Multipart state error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка обработки формы. Пожалуйста, попробуйте еще раз.");
            return "redirect:/products/create";
        }
        throw e; // Re-throw if not multipart related
    }
}