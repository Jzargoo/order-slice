package com.jzargo.inventorymicroservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.FailedReservationCommand;
import com.jzargo.core.messages.command.InventoryReservationCommand;
import com.jzargo.core.messages.command.SuccessfulReservationCommand;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.inventorymicroservice.config.KafkaConfig;
import com.jzargo.inventorymicroservice.exeption.OutOfStockException;
import com.jzargo.inventorymicroservice.repository.ProcessingMessageRepository;
import com.jzargo.inventorymicroservice.service.InventoryService;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderSagaHandler {
    private final InventoryService inventoryService;
    private final ProcessingMessageRepository processingMessageRepository;
    private final MessageTypeRegistry messageTypeRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutboxRepository outboxRepository;

    public OrderSagaHandler(InventoryService inventoryService,
                            ProcessingMessageRepository processingMessageRepository, MessageTypeRegistry messageTypeRegistry, OutboxRepository outboxRepository) {
        this.inventoryService = inventoryService;
        this.processingMessageRepository = processingMessageRepository;
        this.messageTypeRegistry = messageTypeRegistry;
        this.outboxRepository = outboxRepository;
    }

    @SneakyThrows
    @KafkaListener(topics = KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND,
            groupId = KafkaConfig.GROUP_ID)
    public void handleOrderCommand(InventoryReservationCommand reservationCommand,
                                   @Header(name = KafkaConfig.MESSAGE_ID) String messageId
                                   ) {
        if (messageId == null || messageId.isEmpty() ||
                processingMessageRepository.existsById(messageId)) {
            log.error("Message ID is null, empty, or already processed: {}", messageId);
            return;
        }

        log.info("Received order command: {}", reservationCommand);

        Outbox.OutboxBuilder outboxBuilder = Outbox.builder()
                .topicName(KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND_REPLY)
                .key(
                        reservationCommand
                                .getOrderId().toString()
                );

        try {
            inventoryService.reserveProducts(reservationCommand);

            SuccessfulReservationCommand cmd = new SuccessfulReservationCommand(
                    reservationCommand.getOrderId()
            );

            String messageTypeName = messageTypeRegistry.getMessageTypeName(cmd.getClass());

            outboxBuilder.messageType(messageTypeName);
            outboxBuilder.topicName(KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND_REPLY);
            outboxBuilder.payload(
                    objectMapper.writeValueAsString(cmd)
            );


        log.info("Order command processed successfully: {}", reservationCommand);

        }catch (OutOfStockException e){
            log.error("Out of stock for reservation command: {}", reservationCommand, e);


            String errorMessage = "Out of stock for product: " +
                    e.getMessage() +
                    " for order ID: " +
                    reservationCommand.getOrderId();

            outboxBuilder.messageType(
                    messageTypeRegistry.getMessageTypeName(FailedReservationCommand.class)
            );

            outboxBuilder.payload(
                    objectMapper.writeValueAsString(
                            new FailedReservationCommand(
                                    reservationCommand.getOrderId(),
                                    errorMessage)
                    )
            );


        } catch (Exception e) {
            log.error("Error processing order command: {}", reservationCommand, e);

            FailedReservationCommand failedReservationCommand =
                    new FailedReservationCommand(reservationCommand.getOrderId(),
                            "Failed to process reservation command: " + e.getMessage());

            outboxBuilder.messageType(
                    messageTypeRegistry.getMessageTypeName(FailedReservationCommand.class)
            );

            outboxBuilder.payload(
                    objectMapper.writeValueAsString(failedReservationCommand)
            );

        } finally {
            outboxRepository.save(outboxBuilder.build());
        }
    }
}