package com.jzargo.outboxMessaging.outbox.service;

import com.jzargo.outboxMessaging.outbox.Handlers.DispatcherOutbox;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class OutboxServiceImpl implements OutboxService{
    private final OutboxRepository outboxRepository;
    private final DispatcherOutbox dispatcherOutbox;
    public OutboxServiceImpl(OutboxRepository outboxRepository, DispatcherOutbox dispatcherOutbox) {
        this.outboxRepository = outboxRepository;
        this.dispatcherOutbox = dispatcherOutbox;
    }

    @Override
    public void processingOutbox() {
        log.info("Starting outbox processing for unprocessed messages...");
        outboxRepository.findByProcessedFalse()
                .stream()
                .peek(outbox -> {
                    try {
                        processOutbox(outbox);
                    } catch (Exception e) {
                        log.error("Error processing outbox message: {}", outbox.getId(), e);
                        throw new RuntimeException("Error processing outbox message: " + outbox.getId(), e);
                    }
                    outbox.setProcessed(true);
                })
                .forEach(outboxRepository::save);
        log.info("Outbox processing completed, all unprocessed messages have been marked as processed.");
    }

    private void processOutbox(Outbox outbox) throws Exception {
        dispatcherOutbox.dispatch(outbox);
    }
}
