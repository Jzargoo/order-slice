package com.jzargo.core.messages.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class InventoryItem {
    private Integer quantity;
    private Long productId;
}
