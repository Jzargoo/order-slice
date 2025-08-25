package com.jzargo.inventorymicroservice.service;

import com.jzargo.core.messages.command.InventoryItem;
import com.jzargo.core.messages.command.InventoryReservationCommand;
import com.jzargo.core.messages.event.OrderCreatedEvent;
import com.jzargo.core.messages.event.ProductCreatedEvent;
import com.jzargo.inventorymicroservice.exeption.OutOfStockException;
import com.jzargo.inventorymicroservice.mapper.ProductCreatedEventMapper;
import com.jzargo.inventorymicroservice.model.Inventory;
import com.jzargo.inventorymicroservice.model.Reservation;
import com.jzargo.inventorymicroservice.model.ReservationStatus;
import com.jzargo.inventorymicroservice.repository.InventoryRepository;
import com.jzargo.inventorymicroservice.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final ProductCreatedEventMapper productCreatedEventMapper;
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryServiceImpl(ProductCreatedEventMapper productCreatedEventMapper,
                                InventoryRepository inventoryRepository,
                                ReservationRepository reservationRepository) {

        this.productCreatedEventMapper = productCreatedEventMapper;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void initializeInventoryFor(ProductCreatedEvent event) {
        log.debug("Starting to create inventory for product: {}", event.getProductId());
        Inventory map = productCreatedEventMapper.map(event);
        inventoryRepository.save(map);
        log.info("Inventory created for product: {}", event.getProductId());
    }

    @Override
    public void reserveProducts(InventoryReservationCommand reservationCommand) {

        log.debug("Starting to reserve products by order id: {}", reservationCommand.getOrderId());

        List<Inventory> inventories = inventoryRepository.findAllById(reservationCommand.getInventoryItems()
                .stream()
                .map(InventoryItem::getProductId)
                .toList());

        if (inventories.isEmpty() || inventories.size() != reservationCommand.getInventoryItems().size()) {
            log.error("Not all products found for reservation command: {}", reservationCommand);
            throw new IllegalArgumentException("Some products not found in inventory");
        }

        inventories.forEach((inventory) -> reservationLogic(reservationCommand, inventory));
    }

    @Override
    public void confirmReservation(OrderCreatedEvent event) {
        log.debug("Confirming reservation for order id: {}", event.getOrderId());

        Long orderId = event.getOrderId();

        Reservation reserve = reservationRepository.findById(orderId)
                .orElseThrow();
        if (reserve.getStatus() != ReservationStatus.PENDING) {
            log.error("Reservation for order id: {} is not in PENDING status, current status: {}", orderId, reserve.getStatus());
            throw new IllegalStateException("Reservation is not in PENDING status");
        }

        reserve.confirmationStatus();
    }

    private void reservationLogic(InventoryReservationCommand reservationCommand, Inventory inventory) {
        InventoryItem item = reservationCommand.getInventoryItems()
                .stream()
                .filter(i -> i.getProductId().equals(inventory.getProductId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found in reservation command: " + inventory.getProductId()));

        if (inventory.getQuantity() < item.getQuantity()) {
            log.error("Not enough stock for product: {}. Requested: {}, Available: {}",
                      inventory.getProductId(), item.getQuantity(), inventory.getQuantity());
            throw new OutOfStockException("Not enough stock for product: " + inventory.getProductId());
        }

        Reservation build = Reservation.builder()
                .quantity(item.getQuantity())
                .productId(item.getProductId())
                .orderId(reservationCommand.getOrderId())
                .build();

        inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
        inventoryRepository.save(inventory);
        reservationRepository.save(build);
    }
}
