package com.example.foodfrontendservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;




@EnableFeignClients
@SpringBootApplication
public class FoodFrontendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodFrontendServiceApplication.class, args);
    }

}
