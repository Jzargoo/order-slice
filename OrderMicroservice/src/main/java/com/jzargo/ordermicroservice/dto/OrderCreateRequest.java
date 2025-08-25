package com.jzargo.ordermicroservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {
    private String customerId;
    private BigDecimal totalPrice;
    @NotNull(message = "order items cannot be null")
    @JsonProperty("orderItems")
    private List<OrderItemsRequest> orderItems;
}
