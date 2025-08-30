package com.jzargo.core.messages.event;

import com.jzargo.core.registry.MessageType;
import lombok.*;

@MessageType("OrderCreatedEvent")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BasicEvent {
    private Long orderId;
    private Long customerId;
    private String orderStatus;
    private String orderDate;
    private String deliveryDate;
    private String paymentMethod;
    private Double totalAmount;
    private String shippingAddress;
    private String billingAddress;
}
