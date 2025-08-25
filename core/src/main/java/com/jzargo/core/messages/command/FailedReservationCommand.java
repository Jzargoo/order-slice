package com.jzargo.core.messages.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FailedReservationCommand extends BasicCommand{
    private Long orderId;
    private String reason;
}
