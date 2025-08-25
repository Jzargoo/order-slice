package com.jzargo.ordermicroservice.repository;

import com.jzargo.ordermicroservice.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaStateRepository extends JpaRepository<SagaState, String> {
    Optional<SagaState> findByOrderId(Long orderId);
}
