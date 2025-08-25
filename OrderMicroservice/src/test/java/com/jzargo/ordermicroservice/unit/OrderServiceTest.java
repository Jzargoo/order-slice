package com.jzargo.ordermicroservice.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.ordermicroservice.dto.OrderCreateRequest;
import com.jzargo.ordermicroservice.dto.OrderItemsRequest;
import com.jzargo.ordermicroservice.mapper.OrderCreateMapper;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.OrderItems;
import com.jzargo.ordermicroservice.model.OrderState;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import com.jzargo.ordermicroservice.repository.ProcessingMessageRepository;
import com.jzargo.ordermicroservice.saga.OrderCreateSaga;
import com.jzargo.ordermicroservice.service.OrderServiceImpl;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    public static final String MESSAGE_ID = "kklkl1klkKGRtkgre";
    public static final String CUSTOMER_ID = "Kfweq2231";

    @Mock
    private OrderCreateMapper orderCreateMapper;
    @Mock
    private OrderCreateSaga orderCreateSaga;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProcessingMessageRepository processingMessageRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageTypeRegistry messageTypeRegistry;

    private OrderServiceImpl orderService;

    @BeforeEach
    public void prepare(){
        orderService = new OrderServiceImpl(orderCreateMapper, orderCreateSaga,
                orderRepository, processingMessageRepository,
                outboxRepository, objectMapper,
                messageTypeRegistry);
    }

    @Test
    public void initProductTest(){
        OrderCreateRequest request = new OrderCreateRequest();

        request.setTotalPrice(BigDecimal.valueOf(100L));
        request.setCustomerId(CUSTOMER_ID);
        request.setOrderItems(
                List.of(
                        new OrderItemsRequest(1L, 2)
                )
        );

        orderService.initOrder(request, CUSTOMER_ID);

        ArgumentCaptor<OrderCreateCommand> captor = ArgumentCaptor.forClass(OrderCreateCommand.class);
        verify(orderCreateSaga, times(1)).startSaga(captor.capture());

        OrderCreateCommand returned = captor.getValue();

        Assertions.assertEquals(BigDecimal.valueOf(100L), returned.getTotalPrice(),
                "Found mismatch in the total price");
        Assertions.assertEquals(CUSTOMER_ID, returned.getCustomerId(),
                "Found mismatch in the customer id");
        Assertions.assertEquals(1, returned.getOrderItems().size(),
                "Found mismatch in the order count items");
        Assertions.assertEquals(1L, returned.getOrderItems().getFirst().getProductId(),
                "Found mismatch in the order items product id");
        Assertions.assertEquals(2, returned.getOrderItems().getFirst().getQuantity(),
                "Found mismatch in the order item quantity");

    }

    @Test
    public void createOrderMessageIdCopyTest() throws JsonProcessingException {
        when(processingMessageRepository.existsById(MESSAGE_ID)).thenReturn(true);
        orderService.createOrder(null, MESSAGE_ID);

        verify(orderCreateMapper, never()).map(any());
        verify(orderRepository, never()).save(any());
        verify(processingMessageRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
        verify(objectMapper, never()).writeValueAsString(any());

    }
    @Test
    public void createOrderTest() throws Exception {

        when(processingMessageRepository.existsById(MESSAGE_ID)).thenReturn(false);

        OrderCreateCommand cmd = OrderCreateCommand.builder()
                .customerId(CUSTOMER_ID)
                .totalPrice(BigDecimal.TEN)
                .orderItems(List.of(new OrderItemCommand(2, 1L)))
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(cmd.getCustomerId())
                .totalPrice(cmd.getTotalPrice())
                .createdAt(LocalDateTime.now())
                .orderItems(List.of(OrderItems.builder().productId(1L).quantity(2).build()))
                .state(OrderState.WAITING_RESERVATION)
                .build();

        when(orderCreateMapper.map(cmd)).thenReturn(mappedOrder);
        when(orderRepository.save(mappedOrder)).thenReturn(mappedOrder);
        when(messageTypeRegistry.getMessageTypeName(cmd.getClass())).thenReturn("OrderCreateCommand");
        when(objectMapper.writeValueAsString(any())).thenReturn("{ \"dummy\": \"json\" }");

        orderService.createOrder(cmd, MESSAGE_ID);

        verify(processingMessageRepository, times(1)).save(any());
        verify(outboxRepository, times(1)).save(argThat(outbox ->
                outbox.getMessageType().equals("OrderCreateCommand") &&
                        outbox.getPayload().contains("dummy")
        ));
    }


}
