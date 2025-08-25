package com.jzargo.core.messages.command;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductValidationFailedCommand extends BasicCommand{
    private String errorMessage;
    private Long orderId;
}
