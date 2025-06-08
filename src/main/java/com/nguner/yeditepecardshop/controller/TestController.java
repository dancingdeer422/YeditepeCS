package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.repository.UserRepository;
import com.nguner.yeditepecardshop.repository.OrderRepository;
import com.nguner.yeditepecardshop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@Log4j2
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @GetMapping("/api/test")
    public String test(){
        return "Application is running!";
    }

    @GetMapping("/api/test/mongodb")
    public Map<String, Object> testMongoDB() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[TestController][testMongoDB] Testing MongoDB connectivity...");

            // Test user count
            long userCount = userRepository.count();
            result.put("userCount", userCount);
            log.info("[TestController][testMongoDB] User count: {}", userCount);

            // Test order count
            long orderCount = orderRepository.count();
            result.put("orderCount", orderCount);
            log.info("[TestController][testMongoDB] Order count: {}", orderCount);

            result.put("status", "SUCCESS");
            result.put("message", "MongoDB connection is working");
            result.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[TestController][testMongoDB] MongoDB test failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("message", "MongoDB connection failed: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @PostMapping("/api/test/create-user")
    public Map<String, Object> testCreateUser() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[TestController][testCreateUser] Testing user creation...");

            User testUser = new User();
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
            testUser.setPassword("password123");

            String response = userService.registerUser(testUser);

            result.put("status", "SUCCESS");
            result.put("message", response);
            result.put("userEmail", testUser.getEmail());
            result.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[TestController][testCreateUser] User creation test failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("message", "User creation failed: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @PostMapping("/api/test/create-admin")
    public Map<String, Object> createAdminUser() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("[TestController][createAdminUser] Creating admin user...");

            // Check if admin already exists
            User existingAdmin = userRepository.findByEmail("admin@yeditepecs.com");
            if (existingAdmin != null) {
                result.put("status", "INFO");
                result.put("message", "Admin user already exists");
                result.put("email", "admin@yeditepecs.com");
                result.put("password", "admin123");
                result.put("timestamp", LocalDateTime.now());
                return result;
            }

            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@yeditepecs.com");

            // Set admin roles
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_ADMIN");
            roles.add("ROLE_CUSTOMER");
            adminUser.setRoles(roles);

            // Set register date
            adminUser.setRegisterDate(LocalDateTime.now());

            // Encrypt password
            adminUser.setPassword(userService.getPasswordEncoder().encode("admin123"));

            // Save directly to repository
            User savedAdmin = userRepository.save(adminUser);

            result.put("status", "SUCCESS");
            result.put("message", "Admin user created successfully!");
            result.put("email", "admin@yeditepecs.com");
            result.put("password", "admin123");
            result.put("userId", savedAdmin.getId());
            result.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[TestController][createAdminUser] Admin creation failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("message", "Admin creation failed: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }


}
