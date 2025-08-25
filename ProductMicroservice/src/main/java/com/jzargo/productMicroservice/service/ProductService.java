package com.jzargo.productMicroservice.service;

import com.jzargo.core.messages.command.ProductValidationCommand;
import com.jzargo.productMicroservice.dto.ProductRequest;
import com.jzargo.productMicroservice.exception.ValidateProductException;

public interface ProductService{
    void createProduct(ProductRequest productRequest) throws Exception;

    void validateProduct(ProductValidationCommand cmd) throws ValidateProductException;
}
