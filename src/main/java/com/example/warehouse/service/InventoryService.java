package com.example.warehouse.service;

import com.example.warehouse.event.LowInventoryEvent;
import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Product;
import com.example.warehouse.repository.InventoryRepository;
import com.example.warehouse.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));
    }

    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product id: " + productId));
    }

    @Transactional
    public Inventory updateInventoryQuantity(Long productId, int quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.setQuantity(quantity);

        // Check if inventory is below threshold and publish event if it is
        checkAndPublishLowInventoryEvent(inventory);

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory addInventoryQuantity(Long productId, int quantityToAdd) {
        Inventory inventory = getInventoryByProductId(productId);

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory reduceInventoryQuantity(Long productId, int quantityToReduce) {
        Inventory inventory = getInventoryByProductId(productId);

        inventory.reduceQuantity(quantityToReduce);

        // Check if inventory is below threshold and publish event if it is
        checkAndPublishLowInventoryEvent(inventory);

        return inventoryRepository.save(inventory);
    }

    public boolean hasSufficientInventory(Long productId, int quantity) {
        return inventoryRepository.hasSufficientInventory(productId, quantity);
    }

    public List<Inventory> getLowInventory() {
        return (List<Inventory>) inventoryRepository.findLowInventory();
    }

    private void checkAndPublishLowInventoryEvent(Inventory inventory) {
        Product product = inventory.getProduct();

        if (inventory.getQuantity() <= product.getThresholdQuantity()) {
            log.debug("Low inventory detected for product: {}, current quantity: {}, threshold: {}",
                    product.getName(), inventory.getQuantity(), product.getThresholdQuantity());

            eventPublisher.publishEvent(new LowInventoryEvent(this, product.getId(), inventory.getId(), inventory.getQuantity(), product.getThresholdQuantity()));
        }
    }
}
