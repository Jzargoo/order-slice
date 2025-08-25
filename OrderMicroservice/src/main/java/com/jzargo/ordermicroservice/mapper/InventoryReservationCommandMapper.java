package com.jzargo.ordermicroservice.mapper;

import com.jzargo.core.mapper.Mapper;
import com.jzargo.core.messages.command.InventoryItem;
import com.jzargo.core.messages.command.InventoryReservationCommand;
import com.jzargo.core.messages.command.ProductValidationSuccessCommandReply;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationCommandMapper implements Mapper<ProductValidationSuccessCommandReply, InventoryReservationCommand> {

    private final OrderRepository orderRepository;

    public InventoryReservationCommandMapper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public InventoryReservationCommand map
            (ProductValidationSuccessCommandReply cmd) {
        Order order = orderRepository.findById(cmd.getOrderId())
                .orElseThrow();
        return InventoryReservationCommand.builder()
                .orderId(order.getId())
                .inventoryItems(
                        order.getOrderItems().stream()
                                .map(item -> new InventoryItem(item.getQuantity(), item.getProductId()))
                                .toList()
                )
                .build();
    }
}
