package com.example.warehouse.event.listener;

import com.example.warehouse.event.ProductCreatedEvent;
import com.example.warehouse.model.Product;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;

    @Async
    @EventListener
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        Long productId = event.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        log.info("Product created event received for product: {}, SKU: {}",
                product.getName(), product.getSku());

        // Additional business logic for product created event
        // For example: update search index, notify marketing department, etc.
    }
}
