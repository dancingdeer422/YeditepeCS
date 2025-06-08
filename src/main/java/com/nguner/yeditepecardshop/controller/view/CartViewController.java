package com.nguner.yeditepecardshop.controller.view;

import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller                        // NOT @RestController → we return view names
@RequiredArgsConstructor
@RequestMapping("/cart")           // page layer lives here
@PreAuthorize("isAuthenticated()") // all require login
public class CartViewController {

    private final CartService cartService;

     //---------- update quantity ----------
    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam String productId,
                                 @RequestParam int quantity,
                                 Authentication auth) throws UserNotFoundException {

        String userId = ((User) auth.getPrincipal()).getId();
        cartService.updateCartItemQuantity(userId, productId, quantity);
        return "redirect:/cart";  // PRG pattern
    }

    // ---------- remove item ----------
    @PostMapping("/remove")
    public String removeItem(@RequestParam String productId,
                             Authentication auth) throws UserNotFoundException {

        String userId = ((User) auth.getPrincipal()).getId();
        cartService.removeItemFromUserCart(userId, productId);
        return "redirect:/cart";
    }

    // ---------- clear cart ----------
    @PostMapping("/clear")
    public String clear(Authentication auth) throws UserNotFoundException {

        String userId = ((User) auth.getPrincipal()).getId();
        cartService.clearUserCart(userId);
        return "redirect:/cart";
    }
}

