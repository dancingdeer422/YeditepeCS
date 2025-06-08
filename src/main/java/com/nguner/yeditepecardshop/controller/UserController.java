package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.dto.UserRegistrationDTO;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import com.nguner.yeditepecardshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDTO registrationDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/my-account";
            }

            User user = new User();
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setEmail(registrationDTO.getEmail());
            user.setPassword(registrationDTO.getPassword());

            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/my-account";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/my-account";
        }
    }

    // Removed custom login method - Spring Security handles this automatically

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Admin endpoint to get all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Admin endpoint to delete a user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/users/{userId}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting user: " + e.getMessage());
        }
    }
}
