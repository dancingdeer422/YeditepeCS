package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Address;
import com.nguner.yeditepecardshop.model.Cart;
import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import com.nguner.yeditepecardshop.service.AddressService;
import com.nguner.yeditepecardshop.service.CartService;
import com.nguner.yeditepecardshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Controller
public class ViewController {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart")        // HTML page    GET /cart
    public String cartPage(Model model, Authentication auth) {
        String userId = ((User) auth.getPrincipal()).getId();
        Cart cart = cartService.fetchUserCart(userId);
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getCartItems());
        // Calculate subtotal
        double subtotal = cart.getCartItems().stream()
            .filter(item -> item.getProduct() != null)
            .mapToDouble(item -> item.getProduct().getBasePrice() * item.getQuantity())
            .sum();
        double shipping = 0.0; // Shipping is always free
        double total = subtotal + shipping;
        System.out.println("[DEBUG] subtotal=" + subtotal + ", shipping=" + shipping + ", total=" + total);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("total", total);
        return "cart";          // templates/cart.html
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")     // HTML page    GET /profile
    public String profilePage(Model model, Authentication auth) {
        String userId = ((User) auth.getPrincipal()).getId();
        
        // Get fresh user data from database (with updated addresses)
        User freshUser = userRepository.findById(userId).orElse(null);
        if (freshUser == null) {
            // Fallback to auth user if not found in DB
            freshUser = (User) auth.getPrincipal();
        }
        
        // Get user's orders
        List<Order> orders = orderService.getUserOrders(userId);
        
        model.addAttribute("user", freshUser);
        model.addAttribute("orders", orders);
        return "profile";       // templates/profile.html
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/add-address")
    public String addAddress(@ModelAttribute Address address, 
                           Authentication auth, 
                           RedirectAttributes redirectAttributes) {
        try {
            String userId = ((User) auth.getPrincipal()).getId();
            addressService.add(userId, address);
            redirectAttributes.addFlashAttribute("success", "Address added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add address: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/edit-address-form")
    public String editAddressForm(@org.springframework.web.bind.annotation.RequestParam("index") int index,
                                  Model model,
                                  Authentication auth) {
        String userId = ((User) auth.getPrincipal()).getId();
        User freshUser = userRepository.findById(userId).orElse(null);
        if (freshUser == null) {
            freshUser = (User) auth.getPrincipal();
        }
        List<Order> orders = orderService.getUserOrders(userId);
        model.addAttribute("user", freshUser);
        model.addAttribute("orders", orders);
        model.addAttribute("editIndex", index);
        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/order/{orderId}")
    public String orderDetailPage(@PathVariable String orderId, Model model, Authentication auth) {
        String userId = ((User) auth.getPrincipal()).getId();

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("error", "Order not found");
            return "redirect:/profile";
        }

        Order order = orderOpt.get();

        // Check if order belongs to user
        if (!order.getUserId().equals(userId)) {
            model.addAttribute("error", "You don't have permission to view this order");
            return "redirect:/profile";
        }

        model.addAttribute("order", order);
        return "order-detail";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/order/{orderId}/cancel")
    public String cancelOrder(@PathVariable String orderId,
                             RedirectAttributes redirectAttributes,
                             Authentication auth) {
        String userId = ((User) auth.getPrincipal()).getId();

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/profile";
        }

        Order order = orderOpt.get();

        // Check if order belongs to user
        if (!order.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to cancel this order");
            return "redirect:/profile";
        }

        Optional<Order> cancelledOrder = orderService.cancelOrder(orderId);
        if (cancelledOrder.isPresent()) {
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Order cannot be cancelled in its current state");
        }

        return "redirect:/order/" + orderId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/edit-address")
    public String editAddress(@org.springframework.web.bind.annotation.RequestParam("index") int index,
                              @ModelAttribute Address address,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            String userId = ((User) auth.getPrincipal()).getId();
            addressService.update(userId, index, address);
            redirectAttributes.addFlashAttribute("success", "Address updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update address: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkout")
    public String placeOrder(@org.springframework.web.bind.annotation.RequestParam("addressIndex") int addressIndex,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        String userId = ((User) auth.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElse((User) auth.getPrincipal());
        if (user.getAddresses() == null || user.getAddresses().size() <= addressIndex) {
            redirectAttributes.addFlashAttribute("error", "Invalid address selected.");
            return "redirect:/checkout";
        }
        try {
            Address shippingAddress = user.getAddresses().get(addressIndex);
            orderService.createOrderFromCart(userId, shippingAddress);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}



