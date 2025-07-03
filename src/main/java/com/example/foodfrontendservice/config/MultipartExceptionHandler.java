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
public class MultipartExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              RedirectAttributes redirectAttributes) {
        log.error("File size exceeded maximum limit: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error",
                "Размер файла превышает максимально допустимый (10MB). Пожалуйста, выберите файл меньшего размера.");
        return "redirect:/products/create";
    }

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException e,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        log.error("Multipart parsing error: {}", e.getMessage(), e);

        String errorMessage;
        if (e.getMessage().contains("FileCountLimitExceededException")) {
            errorMessage = "Слишком много файлов в запросе. Пожалуйста, загрузите только одно изображение.";
        } else if (e.getMessage().contains("FileSizeLimitExceededException")) {
            errorMessage = "Размер файла слишком большой. Максимальный размер: 10MB.";
        } else if (e.getMessage().contains("SizeLimitExceededException")) {
            errorMessage = "Общий размер запроса слишком большой. Максимальный размер: 15MB.";
        } else {
            errorMessage = "Ошибка загрузки файла. Проверьте формат и размер изображения.";
        }

        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/products/create";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e,
                                              RedirectAttributes redirectAttributes) {
        if (e.getMessage().contains("multipart")) {
            log.error("Multipart state error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка обработки формы. Пожалуйста, попробуйте еще раз.");
            return "redirect:/products/create";
        }
        throw e; // Re-throw if not multipart related
    }
}