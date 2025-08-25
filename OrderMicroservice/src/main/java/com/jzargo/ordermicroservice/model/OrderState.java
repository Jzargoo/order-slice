package com.jzargo.ordermicroservice.model;

public enum OrderState {
    PENDING, WAITING_RESERVATION, RESERVED, CANCELED, COMPLETED, FAILED
}
