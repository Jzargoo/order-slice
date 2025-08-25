package com.jzargo.ordermicroservice.service;

import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderCreateCommandReject;
import com.jzargo.ordermicroservice.dto.OrderCreateRequest;

public interface OrderService {
     void initOrder(OrderCreateRequest request, String customerId);

     void createOrder(OrderCreateCommand cmd, String messageId);

    void rejectOrder(OrderCreateCommandReject reject);
}
