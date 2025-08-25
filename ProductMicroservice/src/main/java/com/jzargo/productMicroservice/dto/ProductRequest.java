package com.jzargo.productMicroservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private Integer quantity;
    private String name;
    private String description;
    private String imageBase64;
    private List<String> tags;
    private BigDecimal price;
    private String sellerId;
}
