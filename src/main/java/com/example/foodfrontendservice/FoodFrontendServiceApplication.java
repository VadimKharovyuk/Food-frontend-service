package com.example.foodfrontendservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;



@SpringBootApplication
@EnableFeignClients
public class FoodFrontendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodFrontendServiceApplication.class, args);


    }



//    @Configuration
//    @EnableFeignClients
//    public class FeignConfig {
//
//        @Bean
//        @LoadBalanced
//        public RestTemplate restTemplate() {
//            return new RestTemplate();
//        }
//    }

//1. JwtUtil.java
//2. JwtAuthenticationFilter.java
//3. FeignAuthInterceptor.java
//4. FeignConfig.java
//
//// application.yml
//    jwt:
//    secret: ${JWT_SECRET}
}
