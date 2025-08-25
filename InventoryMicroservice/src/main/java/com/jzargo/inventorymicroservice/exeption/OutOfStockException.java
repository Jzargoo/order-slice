package com.jzargo.inventorymicroservice.exeption;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String s) {
        super(s);
    }
}
