package com.jzargo.productMicroservice.repository;

import com.jzargo.productMicroservice.model.ProcessingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingMessageRepository extends JpaRepository<ProcessingMessage, String> { }
