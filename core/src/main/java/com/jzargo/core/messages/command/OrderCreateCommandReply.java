package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@MessageType("OrderCreateCommandReply")
@NoArgsConstructor
@Data
public class OrderCreateCommandReply extends BasicCommand{
    private Long orderId;
    private String status;
    private String message;
}