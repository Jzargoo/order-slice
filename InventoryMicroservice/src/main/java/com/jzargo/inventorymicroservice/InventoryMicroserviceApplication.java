package com.jzargo.inventorymicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.jzargo.inventorymicroservice",
        "com.jzargo.core",
        "com.jzargo.outboxMessaging"
})
@EnableJpaRepositories(basePackages = {
        "com.jzargo.inventorymicroservice.repository",
        "com.jzargo.outboxMessaging.outbox.repository"
})
@EntityScan(basePackages = {
        "com.jzargo.inventorymicroservice.model",
        "com.jzargo.outboxMessaging.outbox.model"
})
public class InventoryMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryMicroserviceApplication.class, args);
    }

}
