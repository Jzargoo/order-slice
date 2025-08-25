package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@MessageType("OrderItemCommand")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemCommand extends BasicCommand {
    private Integer quantity;
    private Long productId;
}
