package com.jzargo.productMicroservice.mappper;

import com.jzargo.core.mapper.Mapper;
import com.jzargo.productMicroservice.dto.ProductRequest;
import com.jzargo.productMicroservice.model.Product;
import com.jzargo.productMicroservice.model.Tags;
import org.springframework.stereotype.Component;

@Component
public class ProductCreateMapper implements Mapper<ProductRequest, Product>{

    @Override
    public Product map(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .sellerId(productRequest.getSellerId())
                .tags(
                        productRequest.getTags().stream()
                                .map(tag -> Tags.valueOf(tag.trim()))
                                .toList()
                )
                .build();
    }
}
