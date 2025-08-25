package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@MessageType("ProductValidationCommand")
@Builder
public class ProductValidationCommand extends BasicCommand{
    private List<ProductItemCommand> productItems;
    private Long orderId;
    private BigDecimal totalPrice;
}
