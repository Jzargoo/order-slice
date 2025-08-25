package com.jzargo.inventorymicroservice.handler;

import com.jzargo.core.messages.event.ProductCreatedEvent;
import com.jzargo.inventorymicroservice.config.KafkaConfig;
import com.jzargo.inventorymicroservice.model.ProcessingMessage;
import com.jzargo.inventorymicroservice.repository.ProcessingMessageRepository;
import com.jzargo.inventorymicroservice.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@KafkaListener(topics = KafkaConfig.PRODUCT_EVENT_TOPIC, groupId = KafkaConfig.GROUP_ID)
@Component
@Slf4j
public class ProductEventHandler {
    private final InventoryService inventoryService;
    private final ProcessingMessageRepository processingMessageRepository;

    public ProductEventHandler(InventoryService inventoryService, ProcessingMessageRepository processingMessageRepository) {
        this.inventoryService=inventoryService;
        this.processingMessageRepository = processingMessageRepository;
    }

    @KafkaHandler
    public void handleCreateProductEvent(ProductCreatedEvent event,
                                         @Header(name = KafkaConfig.MESSAGE_ID) String messageId) {

        if (messageId == null || messageId.isEmpty() ||
                processingMessageRepository.existsById(messageId)) {
            log.error("Message ID is null, empty, or already processed: {}", messageId);
            return;
        }

        log.info("Received product creation event: {}", event);
        try {
            inventoryService.initializeInventoryFor(event);
            log.info("Inventory initialized for product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Error initializing inventory for product: {}", event.getProductId(), e);
        }
        ProcessingMessage eventModel = ProcessingMessage.builder()
                .type("event")
                .id(messageId)
                .build();

        processingMessageRepository.save(eventModel);
    }
}