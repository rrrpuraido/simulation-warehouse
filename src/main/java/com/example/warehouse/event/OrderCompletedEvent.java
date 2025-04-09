package com.example.warehouse.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCompletedEvent extends ApplicationEvent {

    private final Long orderId;

    public OrderCompletedEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }
}
