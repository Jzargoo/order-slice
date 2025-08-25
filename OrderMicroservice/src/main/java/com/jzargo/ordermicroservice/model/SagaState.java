package com.jzargo.ordermicroservice.model;

import com.jzargo.ordermicroservice.saga.GlobalOrderSagaStep;
import com.jzargo.ordermicroservice.saga.TargetSagaStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "saga_state")
public class SagaState {

    @Id
    private String id;
    private Long orderId;
    private String errorMessage;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private TargetSagaStep currentTargetSagaStep = TargetSagaStep.PRODUCT;

    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private GlobalOrderSagaStep step = GlobalOrderSagaStep.PENDING;

    @Version
    private Long version;

    public void completeSaga() {
        this.step = GlobalOrderSagaStep.COMPLETED;
    }

    public void rejectingSaga(TargetSagaStep previousTargetSagaStep) {
        this.step = GlobalOrderSagaStep.REJECTING;
        currentTargetSagaStep=previousTargetSagaStep;
    }

    public void successStep(TargetSagaStep nextTargetSagaStep){
        this.step=GlobalOrderSagaStep.PENDING;
        currentTargetSagaStep=nextTargetSagaStep;
    }

    public void failSaga() {
        this.step = GlobalOrderSagaStep.FAILED;
    }
}
