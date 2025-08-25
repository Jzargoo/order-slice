package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@MessageType("ProductValidationSuccessCommandReply")
public class ProductValidationSuccessCommandReply extends BasicCommand{
    private Long orderId;
}
