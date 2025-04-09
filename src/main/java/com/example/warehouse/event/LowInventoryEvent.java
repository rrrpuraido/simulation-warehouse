package com.example.warehouse.event;

import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LowInventoryEvent extends ApplicationEvent {
    
    private final Product product;
    private final Inventory inventory;
    private final int currentQuantity;
    private final int thresholdQuantity;
    
    public LowInventoryEvent(Object source, Product product, Inventory inventory) {
        super(source);
        this.product = product;
        this.inventory = inventory;
        this.currentQuantity = inventory.getQuantity();
        this.thresholdQuantity = product.getThresholdQuantity();
    }
}