package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MessageType("InventoryReservationCommand")
public class InventoryReservationCommand extends BasicCommand{
    private Long orderId;
    private List<InventoryItem> inventoryItems;
}
