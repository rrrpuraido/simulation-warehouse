package com.example.warehouse.event;

import com.example.warehouse.model.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPlacedEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}