package com.jzargo.productMicroservice.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.ProductValidationCommand;
import com.jzargo.core.messages.command.ProductValidationFailedCommand;
import com.jzargo.core.messages.command.ProductValidationSuccessCommandReply;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import com.jzargo.productMicroservice.config.KafkaConfig;
import com.jzargo.productMicroservice.exception.ValidateProductException;
import com.jzargo.productMicroservice.model.ProcessingMessage;
import com.jzargo.productMicroservice.repository.ProcessingMessageRepository;
import com.jzargo.productMicroservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class KafkaProductCommandHandler {
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProcessingMessageRepository processingMessageRepository;
    private final ObjectMapper objectMapper;
    private final MessageTypeRegistry messageTypeRegistry;
    private final OutboxRepository outboxRepository;

    public KafkaProductCommandHandler(ProductService productService,
                                      KafkaTemplate<String, Object> kafkaTemplate,
                                      ProcessingMessageRepository processingMessageRepository, ObjectMapper objectMapper, MessageTypeRegistry messageTypeRegistry, MessageTypeRegistry messageTypeRegistry1, OutboxRepository outboxRepository) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.processingMessageRepository = processingMessageRepository;
        this.objectMapper = objectMapper;
        this.messageTypeRegistry = messageTypeRegistry1;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @KafkaListener(topics = KafkaConfig.PRODUCT_VERIFICATION_ORDER_COMMAND, groupId = KafkaConfig.GROUP_ID)
    public void handleKafkaMessage(@Payload ProductValidationCommand cmd,
                                    @Headers String messageId
                                    ) throws JsonProcessingException {
        if (processingMessageRepository.existsById(messageId)) {
            log.info("Message with ID {} already processed, skipping.", messageId);
            return;
        }

        try {
            log.info("Processing ProductValidationCommand for order ID: {}", cmd.getOrderId());
            productService.validateProduct(cmd);
            ProductValidationSuccessCommandReply productValidationSuccessCommandReply = new ProductValidationSuccessCommandReply(
                    cmd.getOrderId()
            );

            Outbox build = Outbox.builder()
                    .key(cmd
                            .getOrderId().toString())
                    .payload(
                            objectMapper.writeValueAsString(productValidationSuccessCommandReply)
                    )
                    .messageType(
                            messageTypeRegistry
                                    .getMessageTypeName(
                                            productValidationSuccessCommandReply.getClass())
                    )
                    .topicName(KafkaConfig.PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND)
                    .build();
            outboxRepository.save(build);
        } catch (ValidateProductException | JsonProcessingException e) {

            ProductValidationFailedCommand productValidationFailed = ProductValidationFailedCommand.builder()
                    .orderId(cmd.getOrderId())
                    .errorMessage("Product validation failed")
                    .build();

            Outbox build = Outbox.builder()
                    .key(cmd
                            .getOrderId().toString())
                    .payload(
                            objectMapper.writeValueAsString(productValidationFailed)
                    )
                    .messageType(
                            messageTypeRegistry
                                    .getMessageTypeName(
                                            productValidationFailed.getClass())
                    )
                    .topicName(KafkaConfig.PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND)
                    .build();

            outboxRepository.save(build);
        } finally {
            saveMessage(messageId);
        }


    }

    public void saveMessage(String messageId) {
        processingMessageRepository.save(
                new ProcessingMessage(messageId,
                        LocalDateTime.now(),
                        "command")
        );
    }
}
