package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.Address;
import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for handling order-related operations.
 * All endpoints require authentication.
 */
@RequiredArgsConstructor
@RestController
@Log4j2
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order from the authenticated user's cart.
     *
     * @param shippingAddress The shipping address for the order
     * @return ResponseEntity containing the created order or an error message
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Address shippingAddress) {
        String userId = getAuthenticatedUserId("createOrder");

        try {
            Order order = orderService.createOrderFromCart(userId, shippingAddress);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            log.error("[OrderController][createOrder] Error creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserNotFoundException e) {
            log.error("[OrderController][createOrder] User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("[OrderController][createOrder] Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Fetches all orders for the authenticated user.
     *
     * @return ResponseEntity containing the user's orders or a message if no orders exist
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<?> getUserOrders() {
        String userId = getAuthenticatedUserId("getUserOrders");

        List<Order> orders = orderService.getUserOrders(userId);

        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("You have no orders.");
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * Fetches a specific order by ID for the authenticated user.
     *
     * @param orderId The ID of the order to fetch
     * @return ResponseEntity containing the order or an error message
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
        String userId = getAuthenticatedUserId("getOrderById");

        Optional<Order> orderOpt = orderService.getOrderById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }

        Order order = orderOpt.get();

        // Ensure the order belongs to the authenticated user or the user is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!order.getUserId().equals(userId) && !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this order");
        }

        return ResponseEntity.ok(order);
    }

    /**
     * Cancels an order if it's in a cancellable state.
     *
     * @param orderId The ID of the order to cancel
     * @return ResponseEntity containing the cancelled order or an error message
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        String userId = getAuthenticatedUserId("cancelOrder");

        Optional<Order> orderOpt = orderService.getOrderById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }

        Order order = orderOpt.get();

        // Ensure the order belongs to the authenticated user or the user is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!order.getUserId().equals(userId) && !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to cancel this order");
        }

        Optional<Order> cancelledOrderOpt = orderService.cancelOrder(orderId);

        if (cancelledOrderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Order cannot be cancelled in its current state");
        }

        return ResponseEntity.ok(cancelledOrderOpt.get());
    }

    /**
     * Admin endpoint to fetch all orders.
     *
     * @return ResponseEntity containing all orders or a message if no orders exist
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No orders found");
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * Admin endpoint to update the status of an order.
     *
     * @param orderId The ID of the order to update
     * @param status The new status for the order
     * @return ResponseEntity containing the updated order or an error message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        Optional<Order> updatedOrderOpt = orderService.updateOrderStatus(orderId, status);

        if (updatedOrderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }

        return ResponseEntity.ok(updatedOrderOpt.get());
    }

    /**
     * Gets the authenticated user's ID from the security context.
     * This method is used by all endpoints to get the current user's ID.
     *
     * @param methodName The name of the method calling this method (for logging)
     * @return The authenticated user's ID
     * @throws IllegalStateException if the user is not authenticated or the principal is invalid
     */
    private String getAuthenticatedUserId(String methodName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[OrderController][{}] Unauthorized access attempt.", methodName);
            throw new IllegalStateException("User is not authenticated.");
        }
        if (!(authentication.getPrincipal() instanceof User)) {
            log.warn("[OrderController][{}] Invalid principal type.", methodName);
            throw new IllegalStateException("Invalid user principal.");
        }
        User user = (User) authentication.getPrincipal();
        log.info("[OrderController][{}] Authenticated user ID: {}", methodName, user.getId());
        return user.getId();
    }
}
