package com.tmk.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.tmk")
@EntityScan(basePackages = "com.tmk.core")
@EnableJpaRepositories(basePackages = "com.tmk.infra.jpa")
public class TmkApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmkApiApplication.class, args);
    }
}
