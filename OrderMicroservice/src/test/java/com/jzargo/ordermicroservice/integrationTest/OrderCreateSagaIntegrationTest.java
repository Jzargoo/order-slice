package com.jzargo.ordermicroservice.integrationTest;

import com.jzargo.core.messages.command.OrderCreateCommandReply;
import com.jzargo.core.messages.command.ProductValidationSuccessCommandReply;
import com.jzargo.core.messages.command.SuccessfulReservationCommand;
import com.jzargo.ordermicroservice.config.KafkaConfig;
import com.jzargo.ordermicroservice.model.Order;
import com.jzargo.ordermicroservice.model.ProcessingMessage;
import com.jzargo.ordermicroservice.model.SagaState;
import com.jzargo.ordermicroservice.repository.OrderRepository;
import com.jzargo.ordermicroservice.repository.ProcessingMessageRepository;
import com.jzargo.ordermicroservice.repository.SagaStateRepository;
import com.jzargo.ordermicroservice.saga.GlobalOrderSagaStep;
import com.jzargo.ordermicroservice.saga.TargetSagaStep;
import com.jzargo.outboxMessaging.outbox.SchedulerOutbox;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(
        brokerProperties = {"num.brokers=3"},
        topics = {KafkaConfig.ORDER_REPLY_COMMAND}
)
@Sql(
        scripts= {"/test-order-create.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@ActiveProfiles("test")
public class OrderCreateSagaIntegrationTest {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ProcessingMessageRepository processingMessageRepository;
    @Autowired
    private SagaStateRepository sagaStateRepository;
    @MockitoBean
    private SchedulerOutbox schedulerOutbox;

    @Test
    @Transactional
    public void testOrderSuccessReplyCommand() {
        Order first = orderRepository.findAll().getFirst();

        OrderCreateCommandReply orderCreateCommandReply =
                new OrderCreateCommandReply(first.getId(),"success", "order created successful");

        String uuid = sendHelper(new ProducerRecord<>(
                KafkaConfig.ORDER_REPLY_COMMAND,
                orderCreateCommandReply
                        .getOrderId().toString(),
                orderCreateCommandReply));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
            assertEquals(1, outboxRepository.count(), "Outbox should have 1 record");
            assertEquals(1, processingMessageRepository.count(), "ProcessingMessage should have 1 record");
            assertEquals(1, sagaStateRepository.count(), "SagaState should have 1 record");

            Outbox outbox = outboxRepository.findAll().getFirst();
            assertEquals(KafkaConfig.PRODUCT_VERIFICATION_ORDER_COMMAND, outbox.getTopicName());
            assertTrue(outbox.getPayload().contains("orderId"));

            ProcessingMessage pm = processingMessageRepository.findAll().getFirst();
            assertEquals(uuid, pm.getId());

            SagaState saga = sagaStateRepository.findAll().getFirst();
            assertEquals(first.getId(), saga.getOrderId());
        });
    }

    @Test
    @Transactional
    public void testSuccessProductReplyCommand(){
        Order first = orderRepository.findAll().getFirst();
        ProductValidationSuccessCommandReply cmd =
                new ProductValidationSuccessCommandReply(first.getId());

        String uuid = sendHelper(new ProducerRecord<>(
                KafkaConfig.PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND,
                cmd.getOrderId().toString(),
                cmd
        ));

        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {

                    assertEquals(1, outboxRepository.count(), "Outbox should have 1 record");
                    assertEquals(1, processingMessageRepository.count(), "ProcessingMessage should have 1 record");
                    assertEquals(1, sagaStateRepository.count(), "SagaState should have 1 record");

                    Outbox outbox = outboxRepository.findAll().getFirst();
                    assertEquals(KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND, outbox.getTopicName());
                    assertTrue(outbox.getPayload().contains("orderId"));

                    ProcessingMessage pm = processingMessageRepository.findAll().getFirst();
                    assertEquals(uuid, pm.getId());

                    SagaState saga = sagaStateRepository.findAll().getFirst();
                    assertEquals(first.getId(), saga.getOrderId());
                    assertEquals(TargetSagaStep.INVENTORY, saga.getCurrentTargetSagaStep());
                });

    }

    @Test
    @Transactional
    public void testSuccessInventoryReplyCommand(){
        Order first = orderRepository.findAll().getFirst();
        SuccessfulReservationCommand cmd =
                new SuccessfulReservationCommand(first.getId());

        sagaStateRepository.saveAndFlush(
                SagaState.builder()
                        .id(String.valueOf(first.getId()))
                        .orderId(first.getId())
                        .currentTargetSagaStep(TargetSagaStep.INVENTORY)
                        .build()
        );

        String uuid = sendHelper(new ProducerRecord<>(
                KafkaConfig.INVENTORY_RESERVATION_ORDER_COMMAND_REPLY,
                cmd.getOrderId().toString(),
                cmd
        ));

        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> {
                            assertEquals(1, outboxRepository.count(),
                                    "Outbox should have 1 record");

                            assertEquals(1, processingMessageRepository.count(),
                                    "ProcessingMessage should have 1 record");
                            SagaState sagaState = sagaStateRepository.findByOrderId(first.getId())
                                    .orElseThrow();
                            assertEquals(GlobalOrderSagaStep.COMPLETED, sagaState.getStep(),
                                    "SagaState must be completed");

                            Outbox outbox = outboxRepository.findAll().getFirst();
                            assertEquals(KafkaConfig.ORDER_EVENTS_TOPIC, outbox.getTopicName());
                            assertTrue(outbox.getPayload().contains("orderId"));

                            ProcessingMessage pm = processingMessageRepository.findAll().getFirst();
                            assertEquals(uuid, pm.getId());

                            SagaState saga = sagaStateRepository.findAll().getFirst();
                            assertEquals(first.getId(), saga.getOrderId());
                            assertEquals(GlobalOrderSagaStep.COMPLETED, saga.getStep());

                        });
    }

    private String sendHelper(ProducerRecord<String, Object> pd) {

        byte[] uuid = UUID.randomUUID().toString().getBytes();

        pd.headers().add(KafkaConfig.MESSAGE_ID_HEADER, uuid);

        kafkaTemplate.send(pd);

        outboxRepository.flush();
        processingMessageRepository.flush();
        sagaStateRepository.flush();
        return new String(uuid);
    }
}
