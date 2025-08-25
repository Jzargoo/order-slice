package com.jzargo.ordermicroservice.integrationTest;


import com.jzargo.core.messages.command.OrderCreateCommand;
import com.jzargo.core.messages.command.OrderItemCommand;
import com.jzargo.ordermicroservice.dto.OrderCreateRequest;
import com.jzargo.ordermicroservice.dto.OrderItemsRequest;
import com.jzargo.ordermicroservice.service.OrderService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
@DirtiesContext
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private Environment environment;

    private ConcurrentMessageListenerContainer<String, OrderCreateCommand>
            createCommandKafkaMessageListenerContainer;
    private BlockingQueue<ConsumerRecord<String, OrderCreateCommand>> records;



    @BeforeAll
    public void setup(){
        try (AdminClient admin = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString())
        )) {
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
                var desc = admin.describeTopics(List.of(environment.getProperty("order-command-topic-name"))).all().get();
                return desc.get(environment.getProperty("order-command-topic-name")).partitions().size() == 3;
            });
        }

        DefaultKafkaConsumerFactory<String, Object> defaultKafkaConsumerFactory =
                new DefaultKafkaConsumerFactory<>(getConfig());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("order-command-topic-name"));
        createCommandKafkaMessageListenerContainer =
                new ConcurrentMessageListenerContainer<>(defaultKafkaConsumerFactory,containerProperties);

        createCommandKafkaMessageListenerContainer.setConcurrency(3);


        records= new LinkedBlockingQueue<>();

        createCommandKafkaMessageListenerContainer
                .setupMessageListener(
                        (MessageListener<String, OrderCreateCommand>) records::add);

        createCommandKafkaMessageListenerContainer.start();
        ContainerTestUtils.waitForAssignment(createCommandKafkaMessageListenerContainer,
                embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private Map<String, Object> getConfig() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),
                JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")
        );
    }

    @Test
    public void testInitOrder_whenDataIsValid_successfulMessage() throws InterruptedException {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(
                "fwe",
                BigDecimal.valueOf(14.50),
                List.of(
                        new OrderItemsRequest(1L, 1)
                )
        );

        orderService.initOrder(orderCreateRequest, UUID.randomUUID().toString());

        ConsumerRecord<String, OrderCreateCommand> poll = records.poll(4, TimeUnit.SECONDS);

        assertNotNull(poll);
        assertNotNull(poll.key());
        assertNotNull(poll.headers().lastHeader("messageId"));

        OrderCreateCommand value = poll.value();

        assertEquals(orderCreateRequest.getCustomerId(), value.getCustomerId());
        assertEquals(orderCreateRequest.getTotalPrice(), value.getTotalPrice());
        for(OrderItemsRequest requestItem: orderCreateRequest.getOrderItems()){
            OrderItemCommand orderItemCommand = value.getOrderItems()
                    .stream()
                    .filter(item ->
                            requestItem.getProductId().equals(item.getProductId()))
                    .findFirst().orElseThrow();
            assertEquals(requestItem.getQuantity(),orderItemCommand.getQuantity());
        }
    }

    @AfterAll
    void stop(){
        createCommandKafkaMessageListenerContainer.stop();
    }
}
