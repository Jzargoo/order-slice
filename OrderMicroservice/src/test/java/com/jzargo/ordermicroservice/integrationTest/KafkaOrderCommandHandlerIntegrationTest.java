package com.jzargo.ordermicroservice.integrationTest;

import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.ordermicroservice.config.KafkaConfig;
import com.jzargo.ordermicroservice.handler.KafkaOrderCommandHandler;
import com.jzargo.ordermicroservice.service.OrderService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka
@ActiveProfiles("test")
public class KafkaOrderCommandHandlerIntegrationTest {
    private static final String CUSTOMER_ID="fwe";
    private static final Long PRODUCT_ID = 1L;
    private static final Integer QUANTITY = 1;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoSpyBean
    private KafkaOrderCommandHandler kafkaOrderCommandHandler;
    @MockitoBean
    private OrderService orderService;
    @Autowired
    private Environment environment;

    @Test
    @Transactional
    @Rollback(true)
    public void consumeValidOrderCommand() throws ExecutionException, InterruptedException {
        doNothing()
                .when(orderService)
                .createOrder(
                        any(OrderCreateCommand.class),
                        any(String.class));

        OrderCreateCommand orderCommand = new OrderCreateCommand(
                CUSTOMER_ID,
                List.of(
                        new OrderItemCommand(QUANTITY, PRODUCT_ID)
                ),
                BigDecimal.TWO
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                environment.getProperty("order-command-topic-name"),
                CUSTOMER_ID,
                orderCommand);
        String mid = UUID.randomUUID().toString();

        producerRecord.headers().add(KafkaConfig.MESSAGE_ID_HEADER, mid.getBytes());

        kafkaTemplate.send(producerRecord).get();

        ArgumentCaptor<OrderCreateCommand> orderCaptor = ArgumentCaptor.forClass(OrderCreateCommand.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(
                kafkaOrderCommandHandler,
                timeout(5000).times(1)
        ).handleCommand(
                orderCaptor.capture(),
                messageCaptor.capture()
        );

        Assertions.assertEquals(orderCommand, orderCaptor.getValue());
        assertEquals(mid, messageCaptor.getValue());
    }
}
