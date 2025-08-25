package com.jzargo.inventorymicroservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "reservation")
public class Reservation {

    @Id
    private Long id;

    private Long productId;
    private Long orderId;
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(1);

    public void confirmationStatus() {
        status = ReservationStatus.CONFIRMED;
    }
}
