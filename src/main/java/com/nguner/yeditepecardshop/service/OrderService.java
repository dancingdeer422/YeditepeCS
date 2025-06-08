package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.*;
import com.nguner.yeditepecardshop.repository.CartRepository;
import com.nguner.yeditepecardshop.repository.OrderRepository;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import com.nguner.yeditepecardshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling order-related operations.
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CartService cartService;
    private final EmailService emailService;

    /**
     * Creates a new order from a user's cart.
     *
     * @param userId The ID of the user creating the order
     * @param shippingAddress The shipping address for the order
     * @return The created order
     * @throws UserNotFoundException If the user ID is invalid
     */
    public Order createOrderFromCart(String userId, Address shippingAddress) throws UserNotFoundException {
        log.info("[OrderService][createOrderFromCart] Creating order for userId: {}", userId);
        
        // Validate cart items before creating order
        String validationMessage = cartService.validateCartItems(userId);
        if (validationMessage != null && !validationMessage.isEmpty()) {
            log.warn("[OrderService][createOrderFromCart] Cart validation issues: {}", validationMessage);
        }
        
        // Fetch the cart
        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
            log.error("[OrderService][createOrderFromCart] Cart is empty for userId: {}", userId);
            throw new IllegalStateException("Cannot create order from empty cart");
        }
        
        Cart cart = cartOpt.get();
        
        // Create order items from cart items
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(cartItem.getProduct().getId());
                    orderItem.setProductName(cartItem.getProduct().getTitle());
                    orderItem.setPrice(cartItem.getProduct().getBasePrice());
                    orderItem.setQuantity(cartItem.getQuantity());
                    
                    // Update product stock
                    Product product = cartItem.getProduct();
                    product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                    productRepo.save(product);
                    
                    return orderItem;
                })
                .collect(Collectors.toList());
        
        // Create the order
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(orderItems);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        
        // Save the order
        Order savedOrder = orderRepo.save(order);

        // Clear the cart
        cartService.clearUserCart(userId);

        // Send order confirmation email
        try {
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isPresent()) {
                emailService.sendOrderConfirmationEmail(userOpt.get(), savedOrder);
            }
        } catch (Exception e) {
            log.error("[OrderService][createOrderFromCart] Failed to send order confirmation email for order: {}", savedOrder.getId(), e);
        }

        log.info("[OrderService][createOrderFromCart] Order created successfully for userId: {}", userId);
        return savedOrder;
    }
    
    /**
     * Retrieves all orders for a user.
     *
     * @param userId The ID of the user whose orders to retrieve
     * @return A list of the user's orders
     */
    public List<Order> getUserOrders(String userId) {
        log.info("[OrderService][getUserOrders] Retrieving orders for userId: {}", userId);
        return orderRepo.findAll().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a specific order by ID.
     *
     * @param orderId The ID of the order to retrieve
     * @return The order, if found
     */
    public Optional<Order> getOrderById(String orderId) {
        log.info("[OrderService][getOrderById] Retrieving order with ID: {}", orderId);
        return orderRepo.findById(orderId);
    }
    
    /**
     * Updates the status of an order.
     *
     * @param orderId The ID of the order to update
     * @param status The new status for the order
     * @return The updated order, if found
     */
    @Transactional
    public Optional<Order> updateOrderStatus(String orderId, String status) {
        log.info("[OrderService][updateOrderStatus] Updating status of order {} to {}", orderId, status);

        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("[OrderService][updateOrderStatus] Order not found with ID: {}", orderId);
            return Optional.empty();
        }

        Order order = orderOpt.get();
        String oldStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = orderRepo.save(order);

        // Send status update email if status actually changed
        if (!oldStatus.equals(status)) {
            try {
                Optional<User> userOpt = userRepo.findById(order.getUserId());
                if (userOpt.isPresent()) {
                    emailService.sendOrderStatusUpdateEmail(userOpt.get(), savedOrder, oldStatus);
                }
            } catch (Exception e) {
                log.error("[OrderService][updateOrderStatus] Failed to send status update email for order: {}", orderId, e);
            }
        }

        return Optional.of(savedOrder);
    }
    
    /**
     * Cancels an order if it's in a cancellable state.
     *
     * @param orderId The ID of the order to cancel
     * @return The cancelled order, if found and cancellable
     */
    @Transactional
    public Optional<Order> cancelOrder(String orderId) {
        log.info("[OrderService][cancelOrder] Attempting to cancel order with ID: {}", orderId);
        
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("[OrderService][cancelOrder] Order not found with ID: {}", orderId);
            return Optional.empty();
        }
        
        Order order = orderOpt.get();
        
        // Check if order is in a cancellable state (only PENDING and PROCESSING can be cancelled)
        if (!"PENDING".equals(order.getStatus()) && !"PROCESSING".equals(order.getStatus())) {
            log.warn("[OrderService][cancelOrder] Order {} cannot be cancelled in status: {}", orderId, order.getStatus());
            return Optional.empty();
        }
        
        // Return items to inventory
        for (OrderItem item : order.getItems()) {
            Optional<Product> productOpt = productRepo.findById(item.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepo.save(product);
            }
        }
        
        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepo.save(order);

        // Send cancellation email
        try {
            Optional<User> userOpt = userRepo.findById(order.getUserId());
            if (userOpt.isPresent()) {
                emailService.sendOrderCancellationEmail(userOpt.get(), cancelledOrder);
            }
        } catch (Exception e) {
            log.error("[OrderService][cancelOrder] Failed to send cancellation email for order: {}", orderId, e);
        }

        return Optional.of(cancelledOrder);
    }
    
    /**
     * Retrieves all orders (admin function).
     *
     * @return A list of all orders
     */
    public List<Order> getAllOrders() {
        log.info("[OrderService][getAllOrders] Retrieving all orders");
        return orderRepo.findAll();
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void updateOrderStatuses() {
        log.info("[OrderService][updateOrderStatuses] Updating order statuses");

        List<Order> orders = getAllOrders();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {
            if ("PROCESSING".equals(order.getStatus())) {
                // Only complete orders that have been processing for at least 2 minutes
                LocalDateTime orderTime = order.getOrderDate();
                if (orderTime != null && orderTime.plusMinutes(2).isBefore(now)) {
                    order.setStatus("COMPLETED");
                    orderRepo.save(order);
                    log.info("[OrderService][updateOrderStatuses] Updated order {} from PROCESSING to COMPLETED after 2 minutes", order.getId());
                }
            }
        }
    }



}