package com.jzargo.inventorymicroservice.repository;

import com.jzargo.inventorymicroservice.model.ProcessingMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingMessageRepository extends JpaRepository<ProcessingMessage, String> {
    boolean existsById(String id);

}
