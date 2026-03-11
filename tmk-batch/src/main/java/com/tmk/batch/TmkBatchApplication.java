package com.tmk.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tmk")
public class TmkBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmkBatchApplication.class, args);
    }
}
