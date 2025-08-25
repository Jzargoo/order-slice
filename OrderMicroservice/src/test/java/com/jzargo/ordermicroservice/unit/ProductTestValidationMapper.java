package com.jzargo.ordermicroservice.unit;

import com.jzargo.core.messages.command.OrderCreateCommandReply;
import com.jzargo.core.messages.command.ProductValidationCommand;
import com.jzargo.ordermicroservice.mapper.ProductValidationCommandMapper;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.OrderItems;
import com.jzargo.ordermicroservice.model.OrderState;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductTestValidationMapper {

    private ProductValidationCommandMapper mapper;
    private static final String CUSTOMER_ID = "GhitkBHgop2113L";
    private static final Long ORDER_ID = 1L;
    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    public void mapper(){
        mapper=new ProductValidationCommandMapper(orderRepository);
    }

    @Test
    public void mapTest(){
        Optional<Order> build = Optional.ofNullable(Order.builder()
                .state(OrderState.PENDING)
                .createdAt(LocalDateTime.now())
                .customerId(CUSTOMER_ID)
                .totalPrice(BigDecimal.valueOf(30L))
                .updatedAt(LocalDateTime.now())
                .orderItems(
                        List.of(
                                new OrderItems(1L, 1L, 3, null)
                        )
                )
                .build());
        build.get()
                .getOrderItems().getFirst()
                .setOrder(build.get());

        when(orderRepository.findById(ORDER_ID)).thenReturn(build);

        ProductValidationCommand map = mapper.map(
                new OrderCreateCommandReply(ORDER_ID, "success",
                        "message successfully created")
        );

        Assertions.assertEquals(ORDER_ID, map.getOrderId(),
                "Order id do not equal to required");
        Assertions.assertEquals(BigDecimal.valueOf(30L), map.getTotalPrice(),
                "Order ttl p do not equal to required");
        Assertions.assertEquals(1L, map.getProductItems().getFirst().getProductId(),
                "Order product id do not equal to required");
        Assertions.assertEquals(3, map.getProductItems().getFirst().getQuantity(),
                "Order quantity do not equal to required");

    }

}
