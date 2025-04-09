package com.example.warehouse.service;

import com.example.warehouse.event.LowInventoryEvent;
import com.example.warehouse.model.Product;
import com.example.warehouse.model.Supplier;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestockService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @EventListener
    public void handleLowInventoryEvent(LowInventoryEvent event) {
        Product product = productRepository.getReferenceById(event.getProductId());
        int currentQuantity = event.getCurrentQuantity();
        int thresholdQuantity = event.getThresholdQuantity();

        log.info("Low inventory detected for product: {}, current quantity: {}, threshold: {}",
                product.getName(), currentQuantity, thresholdQuantity);

        // Intentionally incomplete implementation
        log.info("TODO: implement restock logic");
    }

    private Supplier findSupplierForProduct(Product product) {
        // In a real implementation, this would find a supplier for the product
        return supplierRepository.findAll().stream().findFirst().orElse(null);
    }

    private void createRestockRequest(Product product, Supplier supplier, int quantity) {
        // In a real implementation, this would create a restock request
        log.info("Creating restock request for product: {}, supplier: {}, quantity: {}",
                product.getName(), supplier.getName(), quantity);
    }

    private void notifySupplier(Supplier supplier, Product product, int quantity) {
        // In a real implementation, this would notify the supplier
        log.info("Notifying supplier: {} about low inventory for product: {}, requesting quantity: {}",
                supplier.getName(), product.getName(), quantity);
    }
}
