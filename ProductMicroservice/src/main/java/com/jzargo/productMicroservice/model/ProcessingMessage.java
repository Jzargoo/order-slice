package com.jzargo.productMicroservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name="processing_messages")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingMessage {
    @Id
    private String id;

    private LocalDateTime processedAt;
    private String type;
}
