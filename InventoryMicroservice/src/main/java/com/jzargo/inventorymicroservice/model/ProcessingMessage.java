package com.jzargo.inventorymicroservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_message")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessingMessage {
    @Id
    private String id;

    private LocalDateTime processedAt;
    private String type;
}
