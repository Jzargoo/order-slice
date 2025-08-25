package com.jzargo.ordermicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderCreateCommandReject;
import com.jzargo.core.messages.command.OrderCreateCommandReply;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.ordermicroservice.dto.OrderCreateRequest;
import com.jzargo.ordermicroservice.mapper.OrderCreateMapper;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.OrderState;
import com.jzargo.ordermicroservice.model.ProcessingMessage;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import com.jzargo.ordermicroservice.repository.ProcessingMessageRepository;
import com.jzargo.ordermicroservice.saga.OrderCreateSaga;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderCreateMapper orderCreateMapper;
    private final OrderCreateSaga orderCreateSaga;
    private final OrderRepository orderRepository;
    private final ProcessingMessageRepository processingMessageRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MessageTypeRegistry messageTypeRegistry;

    public OrderServiceImpl(OrderCreateMapper orderCreateMapper, OrderCreateSaga orderCreateSaga,
                            OrderRepository orderRepository,
                            ProcessingMessageRepository processingMessageRepository,
                            OutboxRepository outboxRepository, ObjectMapper objectMapper, MessageTypeRegistry messageTypeRegistry) {

        this.orderCreateMapper = orderCreateMapper;
        this.orderCreateSaga = orderCreateSaga;
        this.orderRepository = orderRepository;
        this.processingMessageRepository = processingMessageRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.messageTypeRegistry = messageTypeRegistry;
    }


    @Override
    public void initOrder(OrderCreateRequest request, String customerId) {
        request.setCustomerId(customerId);

        orderCreateSaga.startSaga(
                OrderCreateCommand.builder()
                        .customerId(request.getCustomerId())
                        .totalPrice(request.getTotalPrice())
                        .orderItems(
                                request.getOrderItems().stream()
                                        .map(item -> OrderItemCommand.builder()
                                                .productId(item.getProductId())
                                                .quantity(item.getQuantity())
                                                .build()
                                        ).toList()
                        )
                        .build()
        );
    }

    @Override
    @Transactional
    public void createOrder(OrderCreateCommand cmd, String messageId) {

        if (processingMessageRepository.existsById(messageId)) {
            log.info("Message with ID {} already processed. Skipping.", messageId);
            return;
        }

        log.info("Creating order with command: {}", cmd);

        Order order = orderCreateMapper.map(cmd);


        ProcessingMessage processingMessage= new ProcessingMessage(
                messageId,
                LocalDateTime.now(),
                "command"
        );

        Order save = orderRepository.save(order);
        processingMessageRepository.save(processingMessage);

        OrderCreateCommandReply reply = new OrderCreateCommandReply(save.getId(),
                OrderState.WAITING_RESERVATION.name(), "This is a reply message");

        try {
            String messageTypeName = messageTypeRegistry.getMessageTypeName(cmd.getClass());

            outboxRepository.save(
                Outbox.builder()
                        .createdAt(LocalDateTime.now())
                        .messageType(messageTypeName)
                        .payload(objectMapper.writeValueAsString(reply))
                        .build()
            );
        } catch (JsonProcessingException e) {
            log.error("Error serializing OrderCreateCommandReply", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rejectOrder(OrderCreateCommandReject reject) {
        log.info("Rejecting order with command: {}", reject);

        Order order = orderRepository.findById(reject.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setState(OrderState.CANCELED);
        orderRepository.save(order);

    }
}
