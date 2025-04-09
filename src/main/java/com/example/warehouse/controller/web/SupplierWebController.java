package com.example.warehouse.controller.web;

import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierWebController {

    private final SupplierService supplierService;

    @GetMapping
    public String getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchTerm,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        
        // Filter by search term if specified
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Simple filtering by name or email
            suppliers = suppliers.stream()
                    .filter(s -> s.getName().toLowerCase().contains(searchTerm.toLowerCase()) || 
                                 s.getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            model.addAttribute("searchTerm", searchTerm);
        }
        
        // Pagination info
        int totalItems = suppliers.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Paginate the suppliers manually
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Supplier> pagedSuppliers = suppliers.subList(start < totalItems ? start : 0, end < totalItems ? end : totalItems);
        
        model.addAttribute("suppliers", pagedSuppliers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        return "suppliers/list";
    }

    @GetMapping("/{id}")
    public String getSupplierById(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        
        // In a real application, you would get additional data like:
        // - Products supplied by this supplier
        // - Purchase orders for this supplier
        // - Last order date
        
        // For now, we'll just add placeholder data
        model.addAttribute("supplier", supplier);
        model.addAttribute("suppliedProductsCount", 0);
        model.addAttribute("purchaseOrdersCount", 0);
        model.addAttribute("lastOrderDate", null);
        
        return "suppliers/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @PostMapping("/create")
    public String createSupplier(@Valid @ModelAttribute("supplier") Supplier supplier,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }

        try {
            supplierService.createSupplier(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier created successfully");
            return "redirect:/suppliers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating supplier: " + e.getMessage());
            return "redirect:/suppliers/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }

    @PostMapping("/{id}/update")
    public String updateSupplier(@PathVariable Long id,
                                @Valid @ModelAttribute("supplier") Supplier supplier,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }

        try {
            supplierService.updateSupplier(id, supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully");
            return "redirect:/suppliers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating supplier: " + e.getMessage());
            return "redirect:/suppliers/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting supplier: " + e.getMessage());
        }
        return "redirect:/suppliers";
    }
    
    // Additional endpoints for supplier-related functionality
    
    @GetMapping("/{id}/products")
    public String getSupplierProducts(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        // In a real application, you would get products supplied by this supplier
        model.addAttribute("products", List.of());
        return "suppliers/products";
    }
    
    @GetMapping("/{id}/orders")
    public String getSupplierOrders(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        // In a real application, you would get purchase orders for this supplier
        model.addAttribute("orders", List.of());
        return "suppliers/orders";
    }
}