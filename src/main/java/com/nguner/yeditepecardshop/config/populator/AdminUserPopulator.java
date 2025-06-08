package com.nguner.yeditepecardshop.config.populator;

import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(2) // ProductPopulator is Order(1), so this runs after
public class AdminUserPopulator {
    private static final Logger log = LogManager.getLogger(AdminUserPopulator.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserPopulator(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        log.info("[AdminUserPopulator] Checking for admin user...");
        
        // Check if admin user already exists
        User existingAdmin = userRepository.findByEmail("admin@yeditepecs.com");
        if (existingAdmin != null) {
            log.info("[AdminUserPopulator] Admin user already exists with email: admin@yeditepecs.com");
            return;
        }

        // Create admin user
        log.info("[AdminUserPopulator] Creating default admin user...");
        
        User adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@yeditepecs.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        
        // Set admin roles
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_CUSTOMER");
        adminUser.setRoles(roles);
        
        // Set register date
        adminUser.setRegisterDate(LocalDateTime.now());
        
        // Save admin user
        User savedAdmin = userRepository.save(adminUser);
        
        log.info("[AdminUserPopulator] ✅ Admin user created successfully!");
        log.info("[AdminUserPopulator] 📧 Email: admin@yeditepecs.com");
        log.info("[AdminUserPopulator] 🔑 Password: admin123");
        log.info("[AdminUserPopulator] 🆔 User ID: {}", savedAdmin.getId());
        log.info("[AdminUserPopulator] 🛡️ Roles: {}", savedAdmin.getRoles());
    }
}
