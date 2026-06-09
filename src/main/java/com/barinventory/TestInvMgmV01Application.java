package com.barinventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.barinventory") // Removed the .* wildcard
@EntityScan(basePackages = "com.barinventory")             // Removed the .* wildcard
public class TestInvMgmV01Application {

    public static void main(String[] args) {
        SpringApplication.run(TestInvMgmV01Application.class, args);
    }
}