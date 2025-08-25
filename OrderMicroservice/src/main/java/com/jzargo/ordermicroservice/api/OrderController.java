package com.jzargo.ordermicroservice.api;

import com.jzargo.ordermicroservice.dto.OrderCreateRequest;
import com.jzargo.ordermicroservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody @Validated OrderCreateRequest orderCreateRequest) {
        try {
            String customerId = UUID.randomUUID().toString();
            orderService.initOrder(orderCreateRequest, customerId);
        } catch (Exception e) {
            log.error("Failed to create order", e);
            return ResponseEntity.badRequest().body("Failed to create order: " + e.getMessage());
        }
        return ResponseEntity.ok("Order created successfully");
    }
}
