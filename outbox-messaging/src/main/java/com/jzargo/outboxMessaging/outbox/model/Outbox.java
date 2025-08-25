package com.jzargo.outboxMessaging.outbox.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Outbox {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String messageType;
    private String topicName;
    @Column(name = "message_key")
    private String key;
    private LocalDateTime createdAt;
    private Boolean processed;
    private String payload;
}
