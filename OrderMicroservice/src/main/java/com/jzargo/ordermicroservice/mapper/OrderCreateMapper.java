package com.jzargo.ordermicroservice.mapper;

import com.jzargo.core.mapper.Mapper;
import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.OrderItems;
import com.jzargo.ordermicroservice.model.OrderState;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderCreateMapper implements Mapper<OrderCreateCommand, Order> {


    @Override
    public Order map(OrderCreateCommand from) {
        Order build = Order.builder()
                .createdAt(LocalDateTime.now())
                .customerId(from.getCustomerId())
                .totalPrice(from.getTotalPrice())
                .state(OrderState.WAITING_RESERVATION)
                .build();
        for(OrderItemCommand item:from.getOrderItems()){
        build.addOrder(
                OrderItems.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build()
                );
        }
        return build;
    }
}
