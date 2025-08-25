package com.jzargo.ordermicroservice.repository;

import com.jzargo.ordermicroservice.model.ProcessingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingMessageRepository extends JpaRepository<ProcessingMessage, String> {
}
