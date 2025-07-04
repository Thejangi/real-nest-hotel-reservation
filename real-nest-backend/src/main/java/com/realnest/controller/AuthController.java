package com.realnest.controller;

import com.realnest.entity.TravelAgency;
import com.realnest.entity.User;
import com.realnest.repository.TravelAgencyRepository;
import com.realnest.security.JwtTokenProvider;
import com.realnest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TravelAgencyRepository travelAgencyRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> requestBody) {
        try {
            System.out.println("=== REGISTRATION REQUEST RECEIVED ===");
            System.out.println("Request body: " + requestBody);

            String fullName = (String) requestBody.get("fullName");
            String username = (String) requestBody.get("username");
            String email = (String) requestBody.get("email");
            String phone = (String) requestBody.get("phone");
            String password = (String) requestBody.get("password");
            List<String> roles = (List<String>) requestBody.get("roles");

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username is required"));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
            }

            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password must be at least 6 characters"));
            }

            // Register base user
            User savedUser = userService.registerUser(fullName, username, password, email, phone, roles);

            // If Travel Agency role exists, save agency-specific data
            if (roles.contains("AGENCY")) {
                String agencyName = (String) requestBody.get("agencyName");
                Boolean prepaymentRequired = (Boolean) requestBody.getOrDefault("prepaymentRequired", false);
                BigDecimal discountPercentage = new BigDecimal(
                        requestBody.getOrDefault("discountPercentage", 0).toString());

                TravelAgency agency = TravelAgency.builder()
                        .user(savedUser)
                        .name(agencyName)
                        .prepaymentRequired(prepaymentRequired)
                        .discountPercentage(discountPercentage.doubleValue())
                        .build();

                travelAgencyRepository.save(agency);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User registered successfully!",
                    "user", Map.of(
                            "id", savedUser.getId(),
                            "username", savedUser.getUsername(),
                            "email", savedUser.getEmail(),
                            "fullName", savedUser.getFullName())));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Username and password are required"));
            }

            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Invalid credentials"));
            }

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "token", token,
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "roles", user.getRoles())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Auth service is running"));
    }

    @GetMapping("/test")
    public ResponseEntity<?> testAuth() {
        return ResponseEntity.ok(Map.of(
                "message", "Authentication successful!",
                "timestamp", System.currentTimeMillis()));
    }
}
