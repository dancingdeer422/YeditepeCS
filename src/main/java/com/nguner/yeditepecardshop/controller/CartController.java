package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.config.constants.Strings;
import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.Cart;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // Added
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Added
import jakarta.servlet.http.HttpServletRequest; // Added

/**
 * Controller for handling cart-related operations.
 * All endpoints require authentication.
 */
@RequiredArgsConstructor
@Controller // Changed from @RestController
@Log4j2
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    /**
     * Fetches the authenticated user's cart.
     *
     * @return ResponseEntity containing the cart or a message if the cart is empty
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @ResponseBody // Added
    public ResponseEntity<?> fetchUserCart() {
        String userId = getAuthenticatedUserId("fetchUserCart");

        Cart cart = cartService.fetchUserCart(userId);

        if (cart.getCartItems().isEmpty()) {
            log.info("[CartController][fetchUserCart] Cart is empty for user with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Your cart is empty.");
        }

        return ResponseEntity.ok(cart);
    }

    /**
     * Clears all items from the authenticated user's cart.
     *
     * @return ResponseEntity containing a success message or an error message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("isAuthenticated()")
    //@DeleteMapping("/clear")
    @PostMapping("/clear")
    @ResponseBody // Added
    public ResponseEntity<?> clearCart() throws UserNotFoundException {
        String userId = getAuthenticatedUserId("clearCart");

        String result = cartService.clearUserCart(userId);

        if (Strings.CART_IS_EMPTY.equals(result)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Adds a product to the authenticated user's cart.
     *
     * @param productId The ID of the product to add
     * @param quantity The quantity of the product to add
     * @return ResponseEntity containing a success message or an error message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public String addProductToCart(@RequestParam String productId,
                                   @RequestParam int quantity,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) throws UserNotFoundException {
        String userId = getAuthenticatedUserId("addProductToCart");
        String resultMessage = cartService.addItemToUserCart(userId, productId, quantity);

        boolean success = true;

        if (Strings.PRODUCT_NOT_FOUND.equals(resultMessage)) {
            redirectAttributes.addFlashAttribute("cartError", resultMessage);
            success = false;
        } else if (Strings.PRODUCT_OUT_OF_STOCK.equals(resultMessage)) {
            redirectAttributes.addFlashAttribute("cartError", resultMessage);
            success = false;
        } else if (resultMessage != null && resultMessage.contains("not available in requested quantity")) { // Heuristic for quantity error
            redirectAttributes.addFlashAttribute("cartError", resultMessage);
            success = false;
        } else {
            // All other cases are treated as success, resultMessage is the success message
            redirectAttributes.addFlashAttribute("cartSuccess", resultMessage);
        }

        if (success) {
            return "redirect:/cart"; // On success, always go to cart
        } else {
            // On error, redirect back to the page where 'add to cart' was clicked
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty() && !referer.contains("/my-account") && !referer.contains("/api/")) {
                return "redirect:" + referer;
            }
            return "redirect:/"; // Fallback to homepage on error if referer is not good
        }
    }

    /**
     * Updates the quantity of a product in the authenticated user's cart.
     *
     * @param productId The ID of the product to update
     * @param quantity The new quantity of the product
     * @return ResponseEntity containing a success message or an error message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("isAuthenticated()")
    //@PutMapping("/update-quantity")
    @PostMapping("/update-quantity")
    @ResponseBody // Added
    public ResponseEntity<java.util.Map<String, Object>> updateCartItemQuantity(@RequestParam String productId, @RequestParam int quantity) throws UserNotFoundException {
        String userId = getAuthenticatedUserId("updateCartItemQuantity");
        String result = cartService.updateCartItemQuantity(userId, productId, quantity);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        if (result.equals(Strings.CART_UPDATED_SUCCESSFULLY)) {
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Removes a product from the authenticated user's cart.
     *
     * @param productId The ID of the product to remove
     * @return ResponseEntity containing a success message or an error message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("isAuthenticated()")
    //@DeleteMapping("/remove")
    @PostMapping("/remove")
    @ResponseBody // Added
    public ResponseEntity<java.util.Map<String, Object>> removeProductFromCart(@RequestParam String productId) throws UserNotFoundException {
        String userId = getAuthenticatedUserId("removeProductFromCart");
        String result = cartService.removeItemFromUserCart(userId, productId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        if (result.equals(Strings.PRODUCT_REMOVED_FROM_CART)) {
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
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
            log.warn("[CartController][{}] Unauthorized access attempt.", methodName);
            throw new IllegalStateException("User is not authenticated.");
        }
        if (!(authentication.getPrincipal() instanceof User)) {
            log.warn("[CartController][{}] Invalid principal type.", methodName);
            throw new IllegalStateException("Invalid user principal.");
        }
        User user = (User) authentication.getPrincipal();
        log.info("[CartController][{}] Authenticated user ID: {}", methodName, user.getId());
        return user.getId();
    }
}