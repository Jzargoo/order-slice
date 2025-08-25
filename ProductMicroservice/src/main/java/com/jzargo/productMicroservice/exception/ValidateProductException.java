package com.jzargo.productMicroservice.exception;


public class ValidateProductException extends Exception {
    public ValidateProductException() {
        super("Product validation failed");
    }
}
