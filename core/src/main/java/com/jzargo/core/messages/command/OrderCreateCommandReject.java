package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MessageType("OrderCreateCommandReject")
public class OrderCreateCommandReject extends BasicCommand{
    private Long orderId;
}
