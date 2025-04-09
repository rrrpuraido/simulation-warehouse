package com.example.warehouse.event.listener;

import com.example.warehouse.event.LowInventoryEvent;
import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Product;
import com.example.warehouse.repository.InventoryRepository;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryEventListener {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Async
    @EventListener
    public void handleLowInventoryEvent(LowInventoryEvent event) {
        Long productId = event.getProductId();
        Long inventoryId = event.getInventoryId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + inventoryId));

        log.info("Low inventory event received for product: {}, current quantity: {}, threshold: {}",
                product.getName(), event.getCurrentQuantity(), event.getThresholdQuantity());

        // Additional business logic for low inventory event
        // For example: trigger automatic reordering, notify purchasing department, etc.
    }
}
