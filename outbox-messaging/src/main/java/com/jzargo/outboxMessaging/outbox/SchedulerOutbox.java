package com.jzargo.outboxMessaging.outbox;

import com.jzargo.outboxMessaging.outbox.service.OutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedulerOutbox {
    private final OutboxService outboxService;

    public SchedulerOutbox(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 10_000)
    public void processOutbox() {
        outboxService.processingOutbox();
    }
}
