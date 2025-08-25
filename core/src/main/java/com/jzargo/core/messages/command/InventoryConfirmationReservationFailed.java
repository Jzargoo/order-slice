package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MessageType("InventoryConfirmationReservationFailed")
public class InventoryConfirmationReservationFailed extends BasicCommand{
    private Long orderId;
    private String reason;
}
