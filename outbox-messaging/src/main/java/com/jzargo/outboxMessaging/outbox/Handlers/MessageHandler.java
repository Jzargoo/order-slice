package com.jzargo.outboxMessaging.outbox.Handlers;

import com.jzargo.outboxMessaging.outbox.model.Outbox;

public interface MessageHandler {
    void handle(Outbox payload) throws Exception;
}
