package com.jzargo.productMicroservice.config;

import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String PRODUCT_EVENT_TOPIC = "product-event-topic";

    // Name so long because of multiple potential sagas, e.g. product verification is not the only one
    public static final String PRODUCT_VERIFICATION_ORDER_COMMAND = "product-verification-order-command";
    public static final String PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND = "product-verification-order-reply-command";

    public static final String MESSAGE_ID_HEADER = "message-id";
    public static final String GROUP_ID = "product-verification-group";


    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        return new JpaTransactionManager(emf);
    }

    @Bean
    NewTopic orderEventTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    @Bean
    NewTopic productCommandTopic() {
        return TopicBuilder.name(PRODUCT_VERIFICATION_ORDER_COMMAND)
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

    @Bean
    NewTopic productCommandReplyTopic() {
        return TopicBuilder.name(PRODUCT_VERIFICATION_ORDER_REPLY_COMMAND)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }
}
