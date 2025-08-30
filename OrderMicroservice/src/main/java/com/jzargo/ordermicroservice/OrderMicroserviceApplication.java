package com.jzargo.ordermicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "com.jzargo.ordermicroservice",
        "com.jzargo.core",
        "com.jzargo.outboxMessaging"
})
@EnableJpaRepositories(basePackages = {
        "com.jzargo.ordermicroservice.repository",
        "com.jzargo.outboxMessaging.outbox.repository"
})
@EntityScan(basePackages = {
        "com.jzargo.ordermicroservice.model",
        "com.jzargo.outboxMessaging.outbox.model"
})
public class OrderMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMicroserviceApplication.class, args);
    }

}
