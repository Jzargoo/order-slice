package com.jzargo.productMicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "com.jzargo.productMicroservice",
        "com.jzargo.core",
        "com.jzargo.outboxMessaging"
})
@EnableJpaRepositories(basePackages = {
        "com.jzargo.productMicroservice.repository",
        "com.jzargo.outboxMessaging.outbox.repository"
})
@EntityScan(basePackages = {
        "com.jzargo.productMicroservice.model",
        "com.jzargo.outboxMessaging.outbox.model"
})

public class ProductMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductMicroserviceApplication.class, args);
    }

}
