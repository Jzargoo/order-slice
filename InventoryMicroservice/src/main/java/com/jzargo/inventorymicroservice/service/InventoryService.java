package com.jzargo.inventorymicroservice.service;

import com.jzargo.core.messages.command.InventoryReservationCommand;
import com.jzargo.core.messages.event.OrderCreatedEvent;
import com.jzargo.core.messages.event.ProductCreatedEvent;

public interface InventoryService {
    void initializeInventoryFor(ProductCreatedEvent event);

    void reserveProducts(InventoryReservationCommand reservationCommand);

    void confirmReservation(OrderCreatedEvent event);
}
