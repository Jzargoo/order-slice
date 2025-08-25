package com.jzargo.inventorymicroservice.mapper;

import com.jzargo.core.mapper.Mapper;
import com.jzargo.core.messages.event.ProductCreatedEvent;
import com.jzargo.inventorymicroservice.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedEventMapper implements Mapper<ProductCreatedEvent, Inventory> {
    @Override
    public Inventory map(ProductCreatedEvent event) {
        return Inventory.builder()
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .build();
    }
}
