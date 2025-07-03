package com.example.foodfrontendservice.config;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // 🔥 КРИТИЧЕСКИ ВАЖНЫЕ настройки для FileCountLimitExceededException
                connector.setProperty("maxParameterCount", "10000");
                connector.setProperty("maxPostSize", String.valueOf(50 * 1024 * 1024)); // 50MB
                connector.setProperty("maxHttpHeaderSize", "8192"); // 8KB

                // 🔥 САМАЯ ВАЖНАЯ НАСТРОЙКА!
                connector.setProperty("maxFileCount", "20");  // Решает FileCountLimitExceededException

                // Дополнительные настройки
                connector.setProperty("maxThreads", "200");
                connector.setProperty("acceptCount", "100");
                connector.setProperty("connectionTimeout", "20000");
                connector.setProperty("maxSwallowSize", String.valueOf(2 * 1024 * 1024)); // 2MB

                // Настройки для file upload
                connector.setProperty("allowTrace", "false");
                connector.setProperty("asyncTimeout", "30000");

                // Дополнительные настройки для multipart
                connector.setProperty("maxSavePostSize", String.valueOf(50 * 1024 * 1024));
                connector.setProperty("processorCache", "200");
            });
        };
    }
}