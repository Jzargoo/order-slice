package com.jzargo.ordermicroservice.handler;

import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderCreateCommandReject;
import com.jzargo.ordermicroservice.config.KafkaConfig;
import com.jzargo.ordermicroservice.repository.ProcessingMessageRepository;
import com.jzargo.ordermicroservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = KafkaConfig.ORDER_COMMAND, groupId = KafkaConfig.GROUP_ID)
public class KafkaOrderCommandHandler {
    private final OrderService orderService;
    private final ProcessingMessageRepository processingMessageRepository;

    public KafkaOrderCommandHandler(OrderService orderService, ProcessingMessageRepository processingMessageRepository) {
        this.orderService = orderService;
        this.processingMessageRepository = processingMessageRepository;
    }

    @KafkaHandler
    public void handleCommand(@Payload OrderCreateCommand cmd,
                              @Header(KafkaConfig.MESSAGE_ID_HEADER) String messageId) {
        if(processingMessageRepository.existsById(messageId)){
            log.warn("Message with ID {} already processed, skipping.", messageId);
            return;
        }
        log.info("Catching command: {}", cmd);
        orderService.createOrder(cmd, messageId);
    }
    @KafkaHandler
    public void handleFailedCommand(@Payload OrderCreateCommandReject reject,
                                    @Header(KafkaConfig.MESSAGE_ID_HEADER) String messageId
                                    ){
        if(processingMessageRepository.existsById(messageId)){
            log.warn("Message with ID {} already processed, skipping.", messageId);
            return;
        }
        log.info("Catching failed command: {}", reject);
        orderService.rejectOrder(reject);
    }

}
