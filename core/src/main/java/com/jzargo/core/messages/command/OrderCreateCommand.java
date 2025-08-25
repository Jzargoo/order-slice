package com.jzargo.core.messages.command;

import com.jzargo.core.registry.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@MessageType("OrderCreateCommand")
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateCommand extends BasicCommand {
    private String customerId;
    private List<OrderItemCommand> orderItems;
    private BigDecimal totalPrice;

    public static OrderCreateCommandBuilder builder() {
        return new OrderCreateCommandBuilder();
    }

    public static class OrderCreateCommandBuilder {
        private String customerId;
        private List<OrderItemCommand> orderItems;
        private BigDecimal totalPrice;

        OrderCreateCommandBuilder() {
        }

        public OrderCreateCommandBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderCreateCommandBuilder orderItems(List<OrderItemCommand> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public OrderCreateCommandBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public OrderCreateCommand build() {
            return new OrderCreateCommand(this.customerId, this.orderItems, this.totalPrice);
        }

        public String toString() {
            return "OrderCreateCommand.OrderCreateCommandBuilder(customerId=" + this.customerId + ", orderItems=" + this.orderItems + ", totalPrice=" + this.totalPrice + ")";
        }
    }
}
