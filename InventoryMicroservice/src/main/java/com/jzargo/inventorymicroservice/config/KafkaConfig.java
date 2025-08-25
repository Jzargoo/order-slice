package com.jzargo.inventorymicroservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

     public static final String INVENTORY_RESERVATION_ORDER_COMMAND_REPLY
            = "inventory-reservation-order-command-reply";
     public static final String INVENTORY_RESERVATION_ORDER_COMMAND
            = "inventory-reservation-order-command";
     public static final String PRODUCT_EVENT_TOPIC = "product-event-topic";
    public static final String GROUP_ID = "inventory-group";
    public static final String MESSAGE_ID = "message_id";
    public static final String ORDER_EVENT_TOPIC = "order-events";

    @Bean
    NewTopic inventoryCommandTopic(){
        return TopicBuilder.name(INVENTORY_RESERVATION_ORDER_COMMAND)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    @Bean
    NewTopic inventoryCommandReplyTopic(){
        return TopicBuilder.name(INVENTORY_RESERVATION_ORDER_COMMAND_REPLY)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    @Bean
    NewTopic productEventTopic() {
        return TopicBuilder.name(PRODUCT_EVENT_TOPIC)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

}
