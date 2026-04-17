package com.tmk.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tmk")
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.tmk.core")
@EntityScan(basePackages = "com.tmk.core")
public class TmkBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmkBatchApplication.class, args);
    }
}
