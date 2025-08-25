package com.jzargo.ordermicroservice.unit;

import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.ordermicroservice.mapper.OrderCreateMapper;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.OrderItems;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderMapperTest {

    public static final String CUSTOMER_ID = "lfewfweqq";

    private OrderCreateMapper mapper;

    @BeforeEach
    public void mapper(){
        mapper=new OrderCreateMapper();
    }

    @Test
    public void mapTest(){
        OrderCreateCommand from = OrderCreateCommand.builder()
                .customerId(CUSTOMER_ID)
                .totalPrice(BigDecimal.TEN)
                .orderItems(
                        List.of(
                                new OrderItemCommand(2, 1L)
                        )
                )
                .build();

        Order result= mapper.map(from);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(CUSTOMER_ID, result.getCustomerId());
        Assertions.assertEquals(BigDecimal.TEN, result.getTotalPrice());
        Assertions.assertEquals(1, result.getOrderItems().size());

        OrderItems firstItem = result.getOrderItems().getFirst();
        Assertions.assertEquals(1L, firstItem.getProductId());
        Assertions.assertEquals(2, firstItem.getQuantity());
    }
}
