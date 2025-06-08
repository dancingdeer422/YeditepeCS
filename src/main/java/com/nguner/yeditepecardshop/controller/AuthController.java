package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import com.nguner.yeditepecardshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final OrderService orderService;

    @GetMapping("/my-account")
    public String myAccountPage(@RequestParam(required = false) String reason,
                                @RequestParam(required = false) String returnUrl,
                                Model model,
                                Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            User user = userRepository.findByEmail(email);
            if (user != null) {
                model.addAttribute("user", user);
                List<Order> orders = orderService.getUserOrders(user.getId());
                model.addAttribute("orders", orders);
            }
        }

        if ("cart_login_required".equals(reason)) {
            model.addAttribute("loginMessage", "Please log in to add products to your cart.");
        } else if ("cart_access_login_required".equals(reason)) {
            model.addAttribute("loginMessage", "Please log in to view your cart.");
        }
        // Spring Security's SavedRequestAwareAuthenticationSuccessHandler should handle redirection
        // based on the original request that triggered the login, or the 'returnUrl' if configured.
        // Explicitly adding returnUrl to model might be redundant if SecurityConfig handles it.
        // However, it can be useful if the login form itself needs to post it back.
        if (returnUrl != null && !returnUrl.isEmpty()) {
            model.addAttribute("returnUrl", returnUrl); 
        }
        return "my-account"; // Assumes your login page template is my-account.html
    }

    @GetMapping("/api/user/authcheck")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> authCheck() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                                  authentication.isAuthenticated() &&
                                  !(authentication.getPrincipal() instanceof String &&
                                    authentication.getPrincipal().equals("anonymousUser"));
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);
        return ResponseEntity.ok(response);
    }
}

