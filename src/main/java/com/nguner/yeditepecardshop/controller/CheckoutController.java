package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Cart;
import com.nguner.yeditepecardshop.model.User; // User modelini import ediyoruz
import com.nguner.yeditepecardshop.service.CartService;
// UserService'e doğrudan ihtiyacımız olmayabilir eğer User principal'dan ID alabiliyorsak
import com.nguner.yeditepecardshop.service.UserService;
import com.nguner.yeditepecardshop.service.OrderService; // Added OrderService
import com.nguner.yeditepecardshop.model.Address; // Added Address model
import com.nguner.yeditepecardshop.model.Order; // Added Order model
import com.nguner.yeditepecardshop.exception.UserNotFoundException; // Added UserNotFoundException
import com.nguner.yeditepecardshop.exception.OrderNotFoundException; // For handling order not found cases
import java.util.Collections; // For empty list if user not found or has no addresses

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Added PostMapping
import org.springframework.web.bind.annotation.RequestParam; // Added RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Added RedirectAttributes
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger; // SLF4J Logger
import org.slf4j.LoggerFactory; // SLF4J LoggerFactory
import org.springframework.web.bind.annotation.PathVariable; // Added PathVariable
import java.util.Optional; // Added Optional

@Controller
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class); // Logger tanımlaması

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService; // Added OrderService

    public CheckoutController(CartService cartService, UserService userService, OrderService orderService) {
        this.cartService = cartService;
        this.userService = userService;
        this.orderService = orderService; // Initialize OrderService
    }

    private String getAuthenticatedUserId(String methodName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("[CheckoutController][{}] User not authenticated. Redirecting to login.", methodName);
            return null; 
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            log.info("[CheckoutController][{}] Authenticated user ID: {}", methodName, user.getId());
            return user.getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // Spring Security'nin UserDetails User objesi ise username'i alırız
            // Bu durumda username'i ID olarak kullanıyorsanız veya UserService ile ID'ye çevirmeniz gerekir
            // Mevcut OrderController'daki yapı User modelini principal olarak bekliyor.
            // Bu projede User modelinin UserDetails'i implemente ettiğini varsayıyorum.
            org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) principal;
            log.warn("[CheckoutController][{}] Principal is Spring's UserDetails. Username: {}. Consider using custom UserDetails for ID.", methodName, springUser.getUsername());
            // Eğer username'i ID olarak kullanmıyorsanız, bu kısmı UserService ile ID'ye çevirecek şekilde güncellemeniz gerekir.
            // Şimdilik bu durumu hata olarak kabul edip null dönebiliriz veya bir exception fırlatabiliriz.
            // Ancak ideal olan, principal'ın sizin User modeliniz olmasıdır.
            // throw new IllegalStateException("Principal is not an instance of custom User model. Cannot retrieve ID directly.");
            log.error("[CheckoutController][{}] Principal is not an instance of custom User model. Found: {}", methodName, principal.getClass().getName());
            return null; // Veya bir hata fırlat
        } else {
            log.error("[CheckoutController][{}] Unknown principal type: {}. Cannot retrieve user ID.", methodName, principal.getClass().getName());
            return null; // Veya bir hata fırlat
        }
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpServletRequest request) {
        String userId = getAuthenticatedUserId("showCheckoutPage");
        
        if (userId == null) {
            // Kullanıcı giriş yapmamışsa veya ID alınamadıysa login sayfasına yönlendir
            return "redirect:/my-account?reason=checkout_login_required&returnUrl=/checkout";
        }

        try {
            User user = userService.findUserById(userId).orElse(null);
            if (user == null) {
                log.error("[CheckoutController][showCheckoutPage] User not found with ID: {}. Redirecting to login.", userId);
                return "redirect:/my-account?reason=user_not_found&returnUrl=/checkout";
            }
            model.addAttribute("userAddresses", user.getAddresses() != null ? user.getAddresses() : Collections.emptyList());

            Cart cart = cartService.fetchUserCart(userId);
            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.info("[CheckoutController][showCheckoutPage] User {} has an empty cart. Redirecting to cart page.", userId);
                // Sepet boşsa, kullanıcıyı sepet sayfasına bir mesajla yönlendirebiliriz.
                return "redirect:/cart?message=empty_checkout"; 
            }
            model.addAttribute("cart", cart);
            model.addAttribute("currentUrl", request.getRequestURI()); 
            return "checkout";
        } catch (Exception e) { // Daha genel bir exception yakalama
            log.error("[CheckoutController][showCheckoutPage] Error retrieving cart for user {}: {}", userId, e.getMessage(), e);
            // Kullanıcı bulunamazsa veya başka bir sorun olursa hata sayfasına veya ana sayfaya yönlendir
            return "redirect:/cart?error=checkoutError"; 
        }
    }

    @PostMapping("/checkout/process")
    public String processCheckout(@RequestParam(name = "addressSelectionMethod") String addressSelectionMethod,
                                @RequestParam(name = "selectedAddressId", required = false) String selectedAddressId,
                                @RequestParam(name = "newStreet", required = false) String newStreet,
                                @RequestParam(name = "newCity", required = false) String newCity,
                                @RequestParam(name = "newState", required = false) String newState,
                                @RequestParam(name = "newZipCode", required = false) String newZipCode,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {

        String userId = getAuthenticatedUserId("processCheckout");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error_message", "User not authenticated. Please log in.");
            return "redirect:/my-account?reason=checkout_login_required&returnUrl=/checkout";
        }

        Address shippingAddress = null;
        User user;
        try {
            user = userService.findUserById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        } catch (UserNotFoundException e) {
            log.error("[CheckoutController][processCheckout] User not found for ID {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error_message", "User session error. Please log in again.");
            return "redirect:/my-account?reason=user_not_found&returnUrl=/checkout";
        }

        if ("EXISTING".equals(addressSelectionMethod)) {
            if (selectedAddressId == null || selectedAddressId.isEmpty()) {
                redirectAttributes.addFlashAttribute("error_message", "Please select a shipping address.");
                return "redirect:/checkout";
            }
            shippingAddress = user.getAddresses().stream()
                    .filter(a -> a.getId().equals(selectedAddressId))
                    .findFirst()
                    .orElse(null);
            if (shippingAddress == null) {
                redirectAttributes.addFlashAttribute("error_message", "Selected address not found. Please choose a valid address or add a new one.");
                return "redirect:/checkout";
            }
        } else if ("NEW".equals(addressSelectionMethod)) {
            if (newStreet == null || newStreet.trim().isEmpty() ||
                newCity == null || newCity.trim().isEmpty() ||
                newState == null || newState.trim().isEmpty() ||
                newZipCode == null || newZipCode.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error_message", "Please fill in all fields for the new shipping address.");
                return "redirect:/checkout";
            }
            shippingAddress = new Address();
            shippingAddress.setStreet(newStreet.trim());
            shippingAddress.setCity(newCity.trim());
            shippingAddress.setDistrict(newState.trim()); // Corrected: setState to setDistrict
            shippingAddress.setZipCode(newZipCode.trim());
            // Country is not used
            // shippingAddress.setCountry("N/A"); 

            // Optionally, save the new address to the user's profile
            // user.getAddresses().add(shippingAddress);
            // userService.saveUser(user); // Assuming UserService has a saveUser method
            // For now, we'll just use it for this order without saving to profile automatically
        } else {
            redirectAttributes.addFlashAttribute("error_message", "Invalid address selection method.");
            return "redirect:/checkout";
        }

        // The shippingAddress should be non-null here if execution reaches this point due to prior checks.
        // If it were null, one of the earlier return statements would have been triggered.

        try {
            // Validate cart before creating order
            Cart cart = cartService.fetchUserCart(userId);
            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.warn("[CheckoutController][processCheckout] User {} attempted to checkout with an empty cart.", userId);
                redirectAttributes.addFlashAttribute("error_message", "Your cart is empty. Please add items to your cart before checking out.");
                return "redirect:/cart";
            }
            String validationMessage = cartService.validateCartItems(userId);
            if (validationMessage != null && !validationMessage.isEmpty()) {
                log.warn("[CheckoutController][processCheckout] Cart validation failed for user {}: {}", userId, validationMessage);
                redirectAttributes.addFlashAttribute("error_message", "Cart validation failed: " + validationMessage + " Please review your cart.");
                return "redirect:/cart";
            }


            Order createdOrder = orderService.createOrderFromCart(userId, shippingAddress);

            if (createdOrder == null || createdOrder.getId() == null) {
                log.error("[CheckoutController][processCheckout] Order creation failed or returned order/ID is null for user {}.", userId);
                redirectAttributes.addFlashAttribute("error_message", "Failed to create order. Please try again or contact support.");
                return "redirect:/checkout";
            }

            log.info("[CheckoutController][processCheckout] Order {} created for user {}. Current status: {}. Proceeding to mock payment.", createdOrder.getId(), userId, createdOrder.getStatus());

            // Simulate payment processing by updating order status
            Optional<Order> updatedOrderOpt = orderService.updateOrderStatus(createdOrder.getId(), "PROCESSING");

            if (updatedOrderOpt.isEmpty()) {
                log.error("[CheckoutController][processCheckout] Failed to update order {} status to PROCESSING for user {}.", createdOrder.getId(), userId);
                redirectAttributes.addFlashAttribute("error_message", "Failed to finalize order payment. Please contact support.");
                return "redirect:/checkout"; // Stay on checkout if payment update fails
            }
            
            Order paidOrder = updatedOrderOpt.get();
            log.info("[CheckoutController][processCheckout] Mock payment successful for order {}. Status set to {}.", paidOrder.getId(), paidOrder.getStatus());

            redirectAttributes.addFlashAttribute("success_message", "Order placed successfully!");
            return "redirect:/order/confirmation/" + paidOrder.getId(); // Redirect to order confirmation page

        } catch (UserNotFoundException e) {
            log.error("[CheckoutController][processCheckout] Error during order creation (UserNotFound): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error_message", "User not found during order processing. Please log in again.");
            return "redirect:/my-account?reason=user_not_found&returnUrl=/checkout";
        } catch (IllegalStateException e) {
            log.error("[CheckoutController][processCheckout] Error during order creation (IllegalState): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error_message", e.getMessage()); // e.g., "Cannot create order from empty cart"
            return "redirect:/cart";
        } catch (OrderNotFoundException e) { // Specific catch for OrderNotFoundException
            log.error("[CheckoutController][processCheckout] Error related to order not being found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error_message", "There was an issue processing your order details. Please try again or contact support.");
            return "redirect:/checkout";
        } catch (Exception e) {
            log.error("[CheckoutController][processCheckout] Unexpected error during order processing for user {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error_message", "An unexpected error occurred while processing your order. Please try again.");
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order/confirmation/{orderId}")
    public String showOrderConfirmationPage(@PathVariable String orderId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String userId = getAuthenticatedUserId("showOrderConfirmationPage");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error_message", "Authentication required to view order confirmation.");
            return "redirect:/my-account?reason=auth_required&returnUrl=/order/confirmation/" + orderId;
        }

        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                log.warn("[CheckoutController][showOrderConfirmationPage] Order not found with ID: {}", orderId);
                // Check if an error_message was passed via flash attributes from a previous redirect (e.g. if order creation failed mid-way)
                if (!model.containsAttribute("error_message")) {
                    model.addAttribute("error_message", "Order not found.");
                }
                return "order-confirmation"; 
            }

            Order order = orderOpt.get();

            // Verify ownership or admin role
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!order.getUserId().equals(userId) && !isAdmin) {
                log.warn("[CheckoutController][showOrderConfirmationPage] User {} attempted to access order {} belonging to user {}. Denying access.", userId, orderId, order.getUserId());
                model.addAttribute("order", null); // Ensure order is not passed to view
                model.addAttribute("error_message", "You do not have permission to view this order.");
                return "order-confirmation";
            }
            
            // Optionally: Check if order status is appropriate for confirmation, e.g., MOCK_PAYMENT_COMPLETED
            // if (!"MOCK_PAYMENT_COMPLETED".equals(order.getStatus())) {
            //     log.warn("[CheckoutController][showOrderConfirmationPage] Order {} has status '{}', which might not be the expected confirmation status.", orderId, order.getStatus());
            //     // model.addAttribute("warning_message", "Order status is " + order.getStatus() + ". If this seems incorrect, please contact support.");
            // }
            
            model.addAttribute("order", order);
            // The success_message from processCheckout's redirectAttributes will be automatically available in the model.
            model.addAttribute("currentUrl", request.getRequestURI());

        } catch (Exception e) {
            log.error("[CheckoutController][showOrderConfirmationPage] Unexpected error retrieving order confirmation for orderId {}: {}", orderId, e.getMessage(), e);
            model.addAttribute("order", null); // Ensure order is not passed to view on error
            model.addAttribute("error_message", "An unexpected error occurred while retrieving your order details. Please try again later.");
        }
        return "order-confirmation";
    }
}
