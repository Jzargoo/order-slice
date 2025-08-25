package com.jzargo.ordermicroservice.saga;

// RejectingSagaStep is used to indicate that the saga is in the process of rejecting an order,
// while failed indicates that the saga has encountered an error and already rejected.
public enum GlobalOrderSagaStep {
    PENDING, FAILED, COMPLETED, REJECTING
}
