package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@Service
@Log4j2
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registerUser(User user) {
        log.info("[UserService][registerUser] Attempting to register user with email: {}", user.getEmail());

        User check = userRepository.findByEmail(user.getEmail());

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.error("[UserService][registerUser] Email is null or empty");
            throw new RuntimeException("Email cannot be null or empty!");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("[UserService][registerUser] Password is null or empty");
            throw new RuntimeException("Password cannot be null or empty!");
        }

        if (check != null) {
            log.warn("[UserService][registerUser] User already exists with email: {}", user.getEmail());
            throw new RuntimeException("User already exists!");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_CUSTOMER"); //ROLE_CUSTOMER, ROLE_ADMIN, ROLE_SALESMANAGER, ROLE_PRODUCTMANAGER
        user.setRoles(roles);

        LocalDateTime temp = LocalDateTime.now();
        user.setRegisterDate(temp);

        log.info("[UserService][registerUser] Saving user to database...");
        User savedUser = userRepository.save(user);
        log.info("[UserService][registerUser] User saved successfully with ID: {}", savedUser.getId());

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            log.error("[UserService][registerUser] Failed to send welcome email to: {}", savedUser.getEmail(), e);
        }

        return "User created successfully";
    }

    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && encoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public Optional<User> findUserById(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return encoder;
    }
}
