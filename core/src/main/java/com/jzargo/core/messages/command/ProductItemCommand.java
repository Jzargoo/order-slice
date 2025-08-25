package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@MessageType("ProductItemCommand")
@NoArgsConstructor
@Builder
public class ProductItemCommand extends BasicCommand{
    private Long productId;
    private Integer quantity;
}
