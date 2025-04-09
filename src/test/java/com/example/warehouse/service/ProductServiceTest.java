package com.example.warehouse.service;

import com.example.warehouse.event.ProductCreatedEvent;
import com.example.warehouse.model.Product;
import com.example.warehouse.repository.InventoryRepository;
import com.example.warehouse.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setSku("SKU001");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setThresholdQuantity(5);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setSku("SKU002");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setThresholdQuantity(10);

        List<Product> expectedProducts = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productService.getAllProducts();

        // Assert
        assertEquals(expectedProducts.size(), actualProducts.size());
        assertEquals(expectedProducts, actualProducts);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        Product expectedProduct = new Product();
        expectedProduct.setId(productId);
        expectedProduct.setName("Test Product");
        expectedProduct.setSku("SKU001");
        expectedProduct.setPrice(new BigDecimal("10.00"));
        expectedProduct.setThresholdQuantity(5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // Act
        Product actualProduct = productService.getProductById(productId);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(expectedProduct, actualProduct);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void createProduct_ShouldCreateProduct_WhenProductIsValid() {
        // Arrange
        Product productToCreate = new Product();
        productToCreate.setName("New Product");
        productToCreate.setSku("SKU003");
        productToCreate.setPrice(new BigDecimal("15.00"));
        productToCreate.setThresholdQuantity(8);

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        savedProduct.setName("New Product");
        savedProduct.setSku("SKU003");
        savedProduct.setPrice(new BigDecimal("15.00"));
        savedProduct.setThresholdQuantity(8);

        when(productRepository.existsBySku(productToCreate.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(inventoryRepository.save(any())).thenReturn(null);

        // Act
        Product createdProduct = productService.createProduct(productToCreate);

        // Assert
        assertNotNull(createdProduct);
        assertEquals(savedProduct.getId(), createdProduct.getId());
        assertEquals(savedProduct.getName(), createdProduct.getName());
        assertEquals(savedProduct.getSku(), createdProduct.getSku());
        verify(productRepository, times(1)).existsBySku(productToCreate.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(inventoryRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(ProductCreatedEvent.class));
    }

    // Intentionally sparse test coverage:
    // Missing tests for:
    // - getProductBySku
    // - updateProduct
    // - deleteProduct
    // - Error cases (product not found, duplicate SKU, etc.)
    // - Edge cases
}
