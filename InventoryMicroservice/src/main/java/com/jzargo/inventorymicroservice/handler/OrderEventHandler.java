package com.jzargo.inventorymicroservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.InventoryConfirmationReservationFailed;
import com.jzargo.core.messages.event.OrderCreatedEvent;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.inventorymicroservice.config.KafkaConfig;
import com.jzargo.inventorymicroservice.model.ProcessingMessage;
import com.jzargo.inventorymicroservice.repository.ProcessingMessageRepository;
import com.jzargo.inventorymicroservice.service.InventoryService;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@KafkaListener(topics = KafkaConfig.ORDER_EVENT_TOPIC, groupId = KafkaConfig.GROUP_ID)
public class OrderEventHandler {
    private final InventoryService inventoryService;
    private final ProcessingMessageRepository processingMessageRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper= new ObjectMapper();
    private final MessageTypeRegistry messageTypeRegistry;

    public OrderEventHandler(InventoryService inventoryService, ProcessingMessageRepository processingMessageRepository, OutboxRepository outboxRepository, MessageTypeRegistry messageTypeRegistry) {
        this.inventoryService = inventoryService;
        this.processingMessageRepository = processingMessageRepository;
        this.outboxRepository = outboxRepository;
        this.messageTypeRegistry = messageTypeRegistry;
    }

    @KafkaHandler
    public void handleOrderCreatedEvent(
            OrderCreatedEvent event,
            @Header(name = KafkaConfig.MESSAGE_ID) String messageId
    ) throws JsonProcessingException {

        if (messageId == null || messageId.isEmpty() ||
            processingMessageRepository.existsById(messageId)
        ) {
            log.error("Message ID is null or empty for event: {}", event);
            return;
        }

        try {
            inventoryService.confirmReservation(event);
        } catch (Exception e){

            log.error(
                    "Error confirming reservation " +
                            "for order: {}. Error: {}", event.getOrderId(), e.getMessage());

            InventoryConfirmationReservationFailed inventoryConfirmationReservationFailed =
                    new InventoryConfirmationReservationFailed(
                            event.getOrderId(),
                            "Cannot confirm reservation, product out of stock");

            outboxRepository.save(
                    Outbox.builder()
                            .topicName(KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND_REPLY)
                            .messageType(messageTypeRegistry.getMessageTypeName(
                                    inventoryConfirmationReservationFailed.getClass()
                            ))
                            .key(
                                    event.getOrderId().toString()
                            )
                            .payload(
                            objectMapper.writeValueAsString(
                                    inventoryConfirmationReservationFailed)
                            )
                            .build()
            );

        } finally {
            processingMessageRepository.save(
                    ProcessingMessage.builder()
                            .type("event")
                            .id(messageId)
                            .build()
            );
        }
    }
}
