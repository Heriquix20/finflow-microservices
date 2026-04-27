package com.finflow.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class FinflowReportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinflowReportsApplication.class, args);
    }
}
