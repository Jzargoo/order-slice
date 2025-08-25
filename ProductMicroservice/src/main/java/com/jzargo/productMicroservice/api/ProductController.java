package com.jzargo.productMicroservice.api;

import com.jzargo.productMicroservice.dto.ProductRequest;
import com.jzargo.productMicroservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
     public ResponseEntity<Void> createProduct(@RequestBody ProductRequest product) {
        try {
            productService.createProduct(product);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Request rejected because {}", String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }
}
