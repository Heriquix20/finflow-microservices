package com.finflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FinflowGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinflowGatewayApplication.class, args);
    }
}
