package com.jzargo.ordermicroservice.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.*;
import com.jzargo.core.messages.event.OrderCreatedEvent;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.ordermicroservice.config.KafkaConfig;
import com.jzargo.ordermicroservice.mapper.InventoryReservationCommandMapper;
import com.jzargo.ordermicroservice.mapper.ProductValidationCommandMapper;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.ProcessingMessage;
import com.jzargo.ordermicroservice.model.SagaState;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import com.jzargo.ordermicroservice.repository.ProcessingMessageRepository;
import com.jzargo.ordermicroservice.repository.SagaStateRepository;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@Transactional
@KafkaListener(topics = {KafkaConfig.ORDER_REPLY_COMMAND,
        KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND_REPLY,
        KafkaConfig.PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND},
        groupId = KafkaConfig.GROUP_ID
)
public class OrderCreateSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProcessingMessageRepository processingMessageRepository;
    private final ObjectMapper objectMapper;
    private final ProductValidationCommandMapper productValidationCommandMapper;
    private final OutboxRepository outboxRepository;
    private final MessageTypeRegistry messageTypeRegistry;
    private final InventoryReservationCommandMapper inventoryReservationCommandMapper;
    private final SagaStateRepository sagaStateRepository;
    private final OrderRepository orderRepository;

    public OrderCreateSaga(KafkaTemplate<String, Object> kafkaTemplate,
                           ProcessingMessageRepository processingMessageRepository,
                           ObjectMapper objectMapper,
                           ProductValidationCommandMapper productValidationCommandMapper,
                           OutboxRepository outboxRepository,
                           MessageTypeRegistry messageTypeRegistry,
                           SagaStateRepository sagaStateRepository,
                           InventoryReservationCommandMapper inventoryReservationCommandMapper, OrderRepository orderRepository) {

        this.kafkaTemplate = kafkaTemplate;
        this.processingMessageRepository = processingMessageRepository;
        this.objectMapper = objectMapper;
        this.productValidationCommandMapper = productValidationCommandMapper;
        this.outboxRepository = outboxRepository;
        this.messageTypeRegistry = messageTypeRegistry;
        this.sagaStateRepository=sagaStateRepository;
        this.inventoryReservationCommandMapper = inventoryReservationCommandMapper;
        this.orderRepository = orderRepository;
    }

    public void startSaga(OrderCreateCommand cmd) {

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.ORDER_COMMAND, cmd.getCustomerId(),cmd
        );

        String uuid = UUID.randomUUID().toString();

        record.headers().add(KafkaConfig.MESSAGE_ID_HEADER, uuid.getBytes());


        CompletableFuture<SendResult<String, Object>> send =
                kafkaTemplate.send(record);

        send.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error sending OrderCreateCommand to Kafka", ex);
            } else {
                log.info("OrderCreateCommand sent successfully: {}", result.getProducerRecord().value());
            }
        });
    }

    @Transactional
    @KafkaHandler
    public void handleOrderReplyCommand(@Payload OrderCreateCommandReply cmd,
                                        @Header("message-id") String messageId
                                        ) {

        if(
                processingMessageRepository.existsById(messageId)
        ) {
            log.info("Message with ID {} already processed. Skipping.", messageId);
            return;
        }

        log.info("Received OrderCreateCommand reply: {}", cmd);

        ProcessingMessage processingMessage = new ProcessingMessage(
                messageId,
                LocalDateTime.now(),
                "command"
        );

        SagaState sagaState = SagaState.builder()
                .orderId(cmd.getOrderId())
                .id(messageId)
                .build();

        ProductValidationCommand map = productValidationCommandMapper.map(cmd);

        saveOutbox(map,
                map.getOrderId().toString(),
                KafkaConfig.PRODUCT_VERIFICATION_ORDER_COMMAND);
        processingMessageRepository.save(processingMessage);
        sagaStateRepository.save(sagaState);
    }

    @Transactional
    @KafkaHandler
    public void handleSuccessProductReplyCommand(@Payload ProductValidationSuccessCommandReply
                                                             productValidationSuccessCommandReply,
                                                 @Header("message-id") String messageId
                                                 ){
        SagaState saga = sagaStateRepository.findByOrderId(
                productValidationSuccessCommandReply.getOrderId()
        ).orElseThrow();

        if(processingMessageRepository.existsById(messageId) ||
                saga.getCurrentTargetSagaStep() != TargetSagaStep.PRODUCT
        ){
            log.error("Message with id {} or saga for product step already processed", messageId);
            return;
        }

        saga.successStep(TargetSagaStep.INVENTORY);

        InventoryReservationCommand map = inventoryReservationCommandMapper.map(productValidationSuccessCommandReply);

        saveOutbox(
                map,
                map.getOrderId().toString(),
                KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND);

    }

    @Transactional
    @KafkaHandler
    public void handleSuccessInventoryReplyCommand(@Payload SuccessfulReservationCommand cmd,
                                     @Header(KafkaConfig.MESSAGE_ID_HEADER) String messageId
                                     ) {
        if(messageId == null || messageId.isEmpty() ||
                processingMessageRepository.existsById(messageId)) {
            log.error("Message ID is null, empty, or already processed: {}", messageId);
            return;
        }
        log.info("Received SuccessfulReservationCommand: {}", cmd);

        sagaStateRepository.findByOrderId(cmd.getOrderId())
                .ifPresentOrElse(
                        saga -> {
                            if (saga.getCurrentTargetSagaStep() != TargetSagaStep.INVENTORY) {
                                log.error("Saga is not in INVENTORY step, current step: {}", saga.getCurrentTargetSagaStep());
                                return;
                            }
                            saga.completeSaga();
                            sagaStateRepository.save(saga);
                        },
                        () -> {
                            log.error("Saga not found for order ID: {}", cmd.getOrderId());
                            throw new IllegalStateException();
                        }
                );

        ProcessingMessage processingMessage = new ProcessingMessage(
                messageId,
                LocalDateTime.now(),
                "command"
        );
        processingMessageRepository.save(processingMessage);
        Order order = orderRepository.findById(cmd.getOrderId())
                .orElseThrow();
        OrderCreatedEvent build = OrderCreatedEvent.builder()
                .orderId(cmd.getOrderId())
                .orderDate(String.valueOf(LocalDateTime.now()))
                .orderStatus("processed")
                .totalAmount(
                        order.getTotalPrice().doubleValue()
                )
                .build();

        saveOutbox(
                build,
                build.getOrderId().toString(),
                KafkaConfig.ORDER_EVENTS_TOPIC);
    }

    @Transactional
    @KafkaHandler
    public void handleFailedProductReplyCommand(@Payload ProductValidationFailedCommand
                                                             productValidationFailedCommandReply,
                                                 @Header("message-id") String messageId
                                                 ) {

        if(processingMessageRepository.existsById(messageId)) {
            log.info("Message with ID {} already processed. Skipping.", messageId);
            return;
        }
        SagaState sagaState = sagaStateRepository
                .findByOrderId(productValidationFailedCommandReply.getOrderId())
                .orElseThrow();

        if(sagaState.getCurrentTargetSagaStep() != TargetSagaStep.PRODUCT) {
            log.error("Saga is not in PRODUCT step, current step: {}", sagaState.getCurrentTargetSagaStep());
            return;
        }

        log.info("Received ProductValidationFailedCommandReply: {}", productValidationFailedCommandReply);

        ProcessingMessage processingMessage = new ProcessingMessage(
                messageId,
                LocalDateTime.now(),
                "command"
        );


        sagaState.rejectingSaga(TargetSagaStep.ORDER);
        sagaState.setErrorMessage(productValidationFailedCommandReply.getErrorMessage());
        sagaStateRepository.save(sagaState);
        processingMessageRepository.save(processingMessage);

        saveOutbox(
                OrderCreateCommandReject.builder()
                        .orderId(
                                productValidationFailedCommandReply
                                        .getOrderId())
                        .build(),
                productValidationFailedCommandReply.getOrderId().toString(),
                KafkaConfig.ORDER_REPLY_COMMAND
        );
    }

    private void saveOutbox(Object command, String key,String topic) {
        try {
            String messageTypeName = messageTypeRegistry.getMessageTypeName(command.getClass());
            Outbox outbox = Outbox.builder()
                    .createdAt(LocalDateTime.now())
                    .messageType(messageTypeName)
                    .topicName(topic)
                    .key(key)
                    .payload(objectMapper.writeValueAsString(command))
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("Error serializing command for Outbox", e);
            throw new IllegalStateException(e);
        }
    }

    @Transactional
    @KafkaHandler
    public void handleFailedInventoryReplyCommand(@Payload FailedReservationCommand failedReservationCommand,
                                                  @Header(KafkaConfig.MESSAGE_ID_HEADER) String messageId) {

        if (messageId == null || messageId.isEmpty() ||
                processingMessageRepository.existsById(messageId)) {
            log.error("Message ID is null, empty, or already processed: {}", messageId);
            return;
        }

        log.info("Received FailedReservationCommand: {}", failedReservationCommand);

        sagaStateRepository.findByOrderId(failedReservationCommand.getOrderId())
                .ifPresentOrElse(
                        saga -> {
                            if (saga.getCurrentTargetSagaStep() != TargetSagaStep.INVENTORY) {
                                log.error("Saga is not in INVENTORY step, current step: {}", saga.getCurrentTargetSagaStep());
                                return;
                            }
                            saga.failSaga();
                            saga.setErrorMessage(failedReservationCommand.getReason());
                            sagaStateRepository.save(saga);
                        },
                        () -> log.error("Saga not found for order ID: {}", failedReservationCommand.getOrderId())
                );

        ProcessingMessage processingMessage = new ProcessingMessage(
                messageId,
                LocalDateTime.now(),
                "command"
        );
        processingMessageRepository.save(processingMessage);
        saveOutbox(
                 new InventoryReservationReject(
                                failedReservationCommand
                                        .getOrderId()
                 ),
                failedReservationCommand.getOrderId().toString(),
                KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND
        );

        saveOutbox(
                OrderCreateCommandReject.builder()
                        .orderId(
                                failedReservationCommand
                                        .getOrderId())
                        .build(),
                failedReservationCommand.getOrderId().toString(),
                KafkaConfig.ORDER_REPLY_COMMAND
        );

    }
}