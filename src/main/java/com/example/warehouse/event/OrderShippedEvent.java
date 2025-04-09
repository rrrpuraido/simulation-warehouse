package com.example.warehouse.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderShippedEvent extends ApplicationEvent {

    private final Long orderId;

    public OrderShippedEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }
}
