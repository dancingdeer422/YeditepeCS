package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.OrderRepository;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import com.nguner.yeditepecardshop.repository.UserRepository;
import com.nguner.yeditepecardshop.service.OrderService;
import com.nguner.yeditepecardshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Admin Dashboard - Main page
     */
    @GetMapping
    public String adminDashboard(Model model) {
        // Get basic statistics
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        
        // Get recent orders
        List<Order> recentOrders = orderService.getAllOrders()
                .stream()
                .limit(5)
                .toList();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("recentOrders", recentOrders);
        
        return "admin/dashboard";
    }

    /**
     * Orders Management Page
     */
    @GetMapping("/orders")
    public String ordersPage(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    /**
     * Order Detail Page
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable String orderId, Model model) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            model.addAttribute("order", orderOpt.get());
            return "admin/order-detail";
        } else {
            return "redirect:/admin/orders?error=Order not found";
        }
    }

    /**
     * Update Order Status
     */
    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable String orderId, 
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> updatedOrder = orderService.updateOrderStatus(orderId, status);
            if (updatedOrder.isPresent()) {
                redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Order not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating order status: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * Users Management Page
     */
    @GetMapping("/users")
    public String usersPage(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    /**
     * Delete User
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Products Management Page
     */
    @GetMapping("/products")
    public String productsPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    /**
     * Add Product Page
     */
    @GetMapping("/products/add")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    /**
     * Edit Product Page
     */
    @GetMapping("/products/{productId}/edit")
    public String editProductPage(@PathVariable String productId, Model model) {
        try {
            Product product = productService.getProductById(productId);
            model.addAttribute("product", product);
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        } catch (Exception e) {
            return "redirect:/admin/products?error=Product not found";
        }
    }

    /**
     * Save Product (Add or Update)
     */
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, 
                            @RequestParam(required = false) String productId,
                            RedirectAttributes redirectAttributes) {
        try {
            if (productId != null && !productId.isEmpty()) {
                // Update existing product
                productService.updateProduct(productId, product);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            } else {
                // Create new product
                productService.createProduct(product);
                redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * Delete Product
     */
    @PostMapping("/products/{productId}/delete")
    public String deleteProduct(@PathVariable String productId, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
