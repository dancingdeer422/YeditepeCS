package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.config.constants.Strings;
import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.Cart;
import com.nguner.yeditepecardshop.model.CartItem;
import com.nguner.yeditepecardshop.model.Product;
import com.nguner.yeditepecardshop.repository.CartRepository;
import com.nguner.yeditepecardshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class CartService {

    private final ProductRepository productRepo;
    private final CartRepository cartRepo;

    /**
     * Recalculates subtotal, shipping cost, and cart total for the given cart.
     *
     * @param cart The cart to update totals for
     */
    private void updateCartTotals(Cart cart) {
        if (cart == null) {
            log.warn("[CartService][updateCartTotals] Attempted to update totals for a null cart.");
            return;
        }
        if (cart.getCartItems() == null) {
            log.warn("[CartService][updateCartTotals] Cart items list is null for cartId {}. Setting totals to 0.", cart.getId());
            cart.setSubtotal(0.0);
            cart.setShippingCost(0.0);
            cart.setCartTotal(0.0);
            return;
        }

        double subtotal = cart.getCartItems().stream()
                .filter(item -> item != null && item.getProduct() != null) // Avoid null items, products, or prices
                .mapToDouble(item -> item.getProduct().getBasePrice() * item.getQuantity())
                .sum();

        // For now, shipping cost is fixed at 0.0. This can be enhanced later.
        double shippingCost = 0.0;

        double cartTotal = subtotal + shippingCost;

        cart.setSubtotal(subtotal);
        cart.setShippingCost(shippingCost);
        cart.setCartTotal(cartTotal);
        log.debug("[CartService][updateCartTotals] Cart totals updated for cartId {}: subtotal={}, shippingCost={}, cartTotal={}", cart.getId(), subtotal, shippingCost, cartTotal);
    }

    /**
     * Fetches a user's cart or creates a new one if none exists.
     *
     * @param userId The ID of the user whose cart to fetch
     * @return The user's cart
     */
    public Cart fetchUserCart(String userId) {
        log.info("[CartService][fetchUserCart] Fetching cart for userId: {}", userId);

        // Fetch the cart
        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            log.info("[CartService][fetchUserCart] No cart found for userId: {}", userId);

            // Create a new cart if none exists
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setCartItems(new ArrayList<>());
            newCart.setSubtotal(0.0);
            newCart.setShippingCost(0.0); // Default shipping cost
            newCart.setCartTotal(0.0);
            cartRepo.save(newCart);
            return newCart;
        }

        Cart cart = cartOpt.get();
        log.info("[CartService][fetchUserCart] Cart retrieved: {}", cart);
        updateCartTotals(cart); // Ensure cart totals are accurate

        log.info("Returned Cart is {}", cart.toString());

        return cart;
    }

    /**
     * Clears all items from a user's cart.
     *
     * @param userId The ID of the user whose cart to clear
     * @return A message indicating the result of the operation
     * @throws UserNotFoundException If the user ID is invalid
     */
    @Transactional
    public String clearUserCart(String userId) throws UserNotFoundException {
        // Validate the presence of user
        if (userId.isEmpty()) {
            log.error("[CartService][clearUserCart] Invalid userId provided: {}", userId);
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        // Fetch user's cart
        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);

        // Validate the presence of the cart of user
        if (cartOpt.isEmpty()) {
            log.info("[CartService][clearUserCart] Cart for user {} is empty.", userId);
            return Strings.CART_IS_EMPTY;
        } else {
            Cart cart = cartOpt.get();
            cart.getCartItems().clear();
            cart.setSubtotal(0.0);
            cart.setShippingCost(0.0); // Default shipping cost
            cart.setCartTotal(0.0);
            cartRepo.save(cart);

            log.info("[CartService][clearUserCart] Cart cleared for userId: {}", userId);

            return Strings.CART_HAS_BEEN_CLEARED;
        }
    }

    /**
     * Adds a product to a user's cart.
     *
     * @param userId The ID of the user whose cart to add to
     * @param productId The ID of the product to add
     * @param quantity The quantity of the product to add
     * @return A message indicating the result of the operation
     * @throws UserNotFoundException If the user ID is invalid
     */
    @Transactional
    public String addItemToUserCart(String userId, String productId, int quantity) throws UserNotFoundException {
        // Validate the presence of user
        if (userId.isEmpty()) {
            log.error("[CartService][addItemToUserCart] Invalid userId provided: {}", userId);
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        Optional<Product> productOpt = productRepo.findById(productId);

        // Product presence checking
        if (productOpt.isEmpty()) {
            return Strings.PRODUCT_NOT_FOUND;
        }

        Product product = productOpt.get();

        // Stock checking for non-positive stock
        if (product.getStockQuantity() <= 0) {
            return Strings.PRODUCT_OUT_OF_STOCK;
        }

        // Additional check for requested quantity being available
        if (quantity > product.getStockQuantity()) {
            return String.format(Strings.PRODUCT_NOT_AVAILABLE_IN_REQUESTED_QUANTITY, product.getStockQuantity());
        }

        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);
        Cart cart;

        if (cartOpt.isEmpty()) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCartItems(new ArrayList<>());
        } else {
            cart = cartOpt.get();
        }

        // Check if the product already exists in the cart
        Optional<CartItem> existingCartItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();

            // Check if the new quantity exceeds available stock
            int newQuantity = existingCartItem.getQuantity() + quantity;
            if (newQuantity > product.getStockQuantity()) {
                return String.format(Strings.PRODUCT_NOT_AVAILABLE_IN_REQUESTED_QUANTITY, product.getStockQuantity());
            }

            // Update the quantity of the existing cart item
            existingCartItem.setQuantity(newQuantity);
        } else {
            // Create a new cart item if it does not already exist
            CartItem cartItem = new CartItem(product, quantity);
            cart.getCartItems().add(cartItem);
        }

        // Recalculate total price of the cart
        updateCartTotals(cart);
        cartRepo.save(cart);

        log.info("[CartService][addItemToUserCart] User with ID {} added {} units of '{}' to their cart.", userId, quantity, product.getTitle());

        return Strings.PRODUCT_ADDED_TO_CART;
    }

    /**
     * Updates the quantity of an item in a user's cart.
     *
     * @param userId The ID of the user whose cart to update
     * @param productId The ID of the product to update
     * @param quantity The new quantity of the product
     * @return A message indicating the result of the operation
     * @throws UserNotFoundException If the user ID is invalid
     */
    @Transactional
    public String updateCartItemQuantity(String userId, String productId, int quantity) throws UserNotFoundException {
        // Validate the presence of user
        if (userId.isEmpty()) {
            log.error("[CartService][updateCartItemQuantity] Invalid userId provided: {}", userId);
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            log.warn("[CartService][updateCartItemQuantity] No cart found for userId: {}", userId);
            return Strings.CART_IS_EMPTY;
        }

        Cart cart = cartOpt.get();

        // Use Predicate to filter cart items by productId, retaining only matching product in the stream.
        Optional<CartItem> cartItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (cartItemOpt.isEmpty()) {
            log.warn("[CartService][updateCartItemQuantity] Product {} not found in cart for userId: {}", productId, userId);
            return Strings.PRODUCT_NOT_IN_CART;
        }

        CartItem cartItem = cartItemOpt.get();
        Product product = cartItem.getProduct();

        if (quantity <= 0) {
            cart.getCartItems().remove(cartItem);
            log.info("[CartService][updateCartItemQuantity] Removed product {} from cart for userId: {}", productId, userId);
        } else if (quantity > product.getStockQuantity()) {
            return String.format(Strings.PRODUCT_NOT_AVAILABLE_IN_REQUESTED_QUANTITY, product.getStockQuantity());
        } else {
            cartItem.setQuantity(quantity);
            log.info("[CartService][updateCartItemQuantity] Updated product {} quantity to {} for userId: {}", productId, quantity, userId);
        }

        updateCartTotals(cart);
        cartRepo.save(cart);

        return Strings.CART_UPDATED_SUCCESSFULLY;
    }

    /**
     * Removes an item from a user's cart.
     *
     * @param userId The ID of the user whose cart to update
     * @param productId The ID of the product to remove
     * @return A message indicating the result of the operation
     * @throws UserNotFoundException If the user ID is invalid
     */
    @Transactional
    public String removeItemFromUserCart(String userId, String productId) throws UserNotFoundException {

        System.out.print("ENTER TO removeItemFromUserCart!");

        // Validate the presence of user
        if (userId.isEmpty()) {
            log.error("[CartService][removeItemFromUserCart] Invalid userId provided: {}", userId);
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        // Fetch the cart for the user
        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            log.info("[CartService][removeItemFromUserCart] Cart not found for userId: {}", userId);
            return Strings.CART_IS_EMPTY;
        }

        Cart cart = cartOpt.get();

        // Find the cart item to be removed
        Optional<CartItem> cartItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (cartItemOpt.isEmpty()) {
            log.info("[CartService][removeItemFromUserCart] Product with ID {} not found in cart for userId: {}", productId, userId);
            return Strings.PRODUCT_NOT_IN_CART;
        }

        // Remove the cart item
        CartItem cartItem = cartItemOpt.get();
        cart.getCartItems().remove(cartItem);

        // Recalculate the total price
        updateCartTotals(cart);

        // Save the updated cart
        cartRepo.save(cart);

        System.out.print("SAVED TO CART FROM removeItemFromUserCart!");

        log.info("[CartService][removeItemFromUserCart] Product with ID {} removed from cart for userId: {}", productId, userId);
        return Strings.PRODUCT_REMOVED_FROM_CART;
    }

    /**
     * Validates the items in a user's cart against current product stock levels.
     * Removes items that are out of stock or exceed available stock.
     *
     * @param userId The ID of the user whose cart to validate
     * @return A message indicating which items were removed, or null if no items were removed
     * @throws UserNotFoundException If the user ID is invalid
     */
    public String validateCartItems(String userId) throws UserNotFoundException {
        // Validate userId
        if (userId == null || userId.isEmpty()) {
            log.error("[CartService][validateCartItems] Invalid userId provided: {}", userId);
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        // Fetch the cart for the user
        Optional<Cart> cartOpt = cartRepo.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            log.info("[CartService][validateCartItems] Cart not found for userId: {}", userId);
            return null; // No cart to validate
        }

        Cart cart = cartOpt.get();

        // Initialize a message for removed items
        StringBuilder messageBuilder = new StringBuilder();
        List<CartItem> validItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() <= 0) {
                log.info("[CartService][validateCartItems] Removing product {} from cart for userId {} as it is out of stock.", product.getId(), userId);
                messageBuilder.append(String.format("'%s' is out of stock. ", product.getTitle()));
                continue;
            }

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                log.info("[CartService][validateCartItems] Removing product {} from cart for userId {} as it exceeds available stock.", product.getId(), userId);
                messageBuilder.append(String.format("'%s' exceeds available stock. ", product.getTitle()));
                continue;
            }

            validItems.add(cartItem);
        }

        // Update the cart with only valid items
        cart.setCartItems(validItems);
        updateCartTotals(cart);
        cartRepo.save(cart);

        // Return the removal message if any items were removed
        String message = messageBuilder.toString();
        if (!message.isEmpty()) {
            log.info("[CartService][validateCartItems] Removed items message: {}", message);
        }
        return message.isEmpty() ? null : message;
    }
}