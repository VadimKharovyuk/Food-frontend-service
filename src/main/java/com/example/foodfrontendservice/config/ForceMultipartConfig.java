// 🔥 СОЗДАЙТЕ ЭТОТ КЛАСС - ОН ПРИНУДИТЕЛЬНО НАСТРОИТ TOMCAT

package com.example.foodfrontendservice.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.unit.DataSize;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ForceMultipartConfig {

    /**
     * 🔥 ПРИНУДИТЕЛЬНАЯ настройка Tomcat для multipart
     */
    @Bean
    @Primary
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> forceMultipartTomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                log.info("🔧 Принудительно настраиваем Tomcat для multipart...");

                // 🔥 КРИТИЧЕСКИЕ настройки для FileCountLimitExceededException
                connector.setProperty("maxParameterCount", "10000");
                connector.setProperty("maxPostSize", String.valueOf(50 * 1024 * 1024)); // 50MB
                connector.setProperty("maxHttpHeaderSize", "8192");

                // 🔥 САМАЯ ВАЖНАЯ НАСТРОЙКА - решает FileCountLimitExceededException
                connector.setProperty("maxFileCount", "50");  // Увеличиваем до 50!

                // Дополнительные настройки для стабильности
                connector.setProperty("maxThreads", "200");
                connector.setProperty("acceptCount", "100");
                connector.setProperty("connectionTimeout", "30000");
                connector.setProperty("maxSwallowSize", String.valueOf(10 * 1024 * 1024)); // 10MB
                connector.setProperty("allowTrace", "false");
                connector.setProperty("asyncTimeout", "60000");

                // Дополнительные настройки для multipart
                connector.setProperty("maxSavePostSize", String.valueOf(50 * 1024 * 1024));
                connector.setProperty("processorCache", "200");
                connector.setProperty("maxParameterCount", "10000");

                log.info("✅ Tomcat настроен: maxFileCount=50, maxPostSize=50MB, maxParameterCount=10000");
            });

            // Дополнительные настройки контекста
            factory.addContextCustomizers(context -> {
                context.setAllowCasualMultipartParsing(true);
                log.info("✅ Включен casual multipart parsing");
            });
        };
    }

    /**
     * 🔥 ПРИНУДИТЕЛЬНАЯ настройка MultipartConfig
     */
    @Bean
    @Primary
    public MultipartConfigElement forceMultipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Увеличиваем все лимиты
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        factory.setFileSizeThreshold(DataSize.ofKilobytes(0));

        log.info("✅ MultipartConfig настроен: maxFileSize=10MB, maxRequestSize=50MB");

        return factory.createMultipartConfig();
    }
}