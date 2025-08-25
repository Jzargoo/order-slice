package com.jzargo.core.messages.event;

import com.jzargo.core.messages.command.BasicCommand;
import com.jzargo.core.registry.MessageType;
import lombok.*;

@MessageType("ProductCreatedEvent")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductCreatedEvent extends BasicCommand {
    private Long productId;
    private Integer quantity;
}
