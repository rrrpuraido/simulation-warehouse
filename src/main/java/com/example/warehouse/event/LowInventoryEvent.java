package com.example.warehouse.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LowInventoryEvent extends ApplicationEvent {

    private final Long productId;
    private final Long inventoryId;
    private final int currentQuantity;
    private final int thresholdQuantity;

    public LowInventoryEvent(Object source, Long productId, Long inventoryId, int currentQuantity, int thresholdQuantity) {
        super(source);
        this.productId = productId;
        this.inventoryId = inventoryId;
        this.currentQuantity = currentQuantity;
        this.thresholdQuantity = thresholdQuantity;
    }
}
