package com.jzargo.ordermicroservice.mapper;

import com.jzargo.core.mapper.Mapper;
import com.jzargo.core.messages.command.OrderCreateCommandReply;
import com.jzargo.core.messages.command.ProductItemCommand;
import com.jzargo.core.messages.command.ProductValidationCommand;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductValidationCommandMapper implements Mapper<OrderCreateCommandReply, ProductValidationCommand> {
    private final OrderRepository orderRepository;

    public ProductValidationCommandMapper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public ProductValidationCommand map(OrderCreateCommandReply from) {
        Order order = orderRepository.findById(from.getOrderId()).orElseThrow();

        return ProductValidationCommand.builder()
                .totalPrice(
                    order.getTotalPrice()
                )
                .orderId(
                        from.getOrderId()
                )
                .productItems(
                    order.getOrderItems().stream()
                        .map(item -> ProductItemCommand.builder()
                            .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build()
                        ).toList()
                )
                .build();
    }
}
