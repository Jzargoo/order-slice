package com.jzargo.outboxMessaging.outbox.Handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageHandlerImpl implements MessageHandler{

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessageTypeRegistry messageTypeRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageHandlerImpl(KafkaTemplate<String, Object> kafkaTemplate, MessageTypeRegistry messageTypeRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageTypeRegistry = messageTypeRegistry;
    }

    @Override
    public void handle(Outbox payload) throws Exception {

        Class<?> messageTypeClass =
                messageTypeRegistry.getMessageTypeClass(payload.getMessageType());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                payload.getTopicName(),
                payload.getKey(),
                objectMapper.readValue(payload.getPayload(), messageTypeClass)
                );
        record.headers().add("message-id", UUID.randomUUID().toString().getBytes());

        kafkaTemplate.send(record);
    }
}
