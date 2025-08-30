package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@MessageType("FailedReservationCommand")
@AllArgsConstructor
@NoArgsConstructor
public class FailedReservationCommand extends BasicCommand{
    private Long orderId;
    private String reason;
}
