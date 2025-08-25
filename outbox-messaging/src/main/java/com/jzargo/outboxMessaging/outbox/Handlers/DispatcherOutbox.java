package com.jzargo.outboxMessaging.outbox.Handlers;

import com.jzargo.outboxMessaging.outbox.model.Outbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DispatcherOutbox {
    private final MessageHandler handler;

    public DispatcherOutbox(MessageHandler handler) {
        this.handler = handler;
    }

    public void dispatch(Outbox outbox) throws Exception {
            handler.handle(outbox);
    }
}
