package com.example.warehouse.controller.web;

import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.OrderItem;
import com.example.warehouse.model.Product;
import com.example.warehouse.service.InventoryService;
import com.example.warehouse.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @GetMapping
    public String getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchTerm,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        List<Product> products = productService.getAllProducts();

        // Filter by search term if specified
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Simple filtering by name or SKU
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                 p.getSku().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            model.addAttribute("searchTerm", searchTerm);
        }

        // No need to add inventory information to products anymore
        // The template has been updated to not access product.inventory

        // Pagination info
        int totalItems = products.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Paginate the products manually
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Product> pagedProducts = products.subList(start < totalItems ? start : 0, end < totalItems ? end : totalItems);

        model.addAttribute("products", pagedProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        return "products/list";
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);

        // Get inventory for this product
        try {
            Inventory inventory = inventoryService.getInventoryByProductId(id);
            model.addAttribute("inventory", inventory);
        } catch (Exception e) {
            // Inventory not found, that's okay
        }

        // Get recent orders for this product
        // In a real application, you would have a method in OrderService to get orders by product
        List<OrderItem> orders = List.of(); // Placeholder

        model.addAttribute("product", product);
        model.addAttribute("orders", orders);

        return "products/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("product") Product product,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            Product savedProduct = productService.createProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating product: " + e.getMessage());
            return "redirect:/products/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        // Get inventory for this product
        try {
            Inventory inventory = inventoryService.getInventoryByProductId(id);
            model.addAttribute("inventory", inventory);
        } catch (Exception e) {
            // Inventory not found, that's okay
        }

        return "products/form";
    }

    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
                               @Valid @ModelAttribute("product") Product product,
                               BindingResult result,
                               @RequestParam(required = false) Integer inventoryQuantity,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            Product updatedProduct = productService.updateProduct(id, product);

            // Update inventory if quantity is provided
            if (inventoryQuantity != null) {
                inventoryService.updateInventoryQuantity(id, inventoryQuantity);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
            return "redirect:/products/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/products";
    }
}
