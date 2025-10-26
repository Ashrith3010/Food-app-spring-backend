package com.food_api.food_api.service;

import com.food_api.food_api.dto.ChangePasswordRequest;
import com.food_api.food_api.dto.UserDTO;
import com.food_api.food_api.dto.credentials.LoginRequest;
import com.food_api.food_api.dto.credentials.LoginResponse;
import com.food_api.food_api.dto.RegisterRequest;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                            .orElseGet(() -> userRepository.findByPhone(request.getUsername())
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found"))));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                LOGGER.warn("Invalid credentials for user: {}", request.getUsername());
                return LoginResponse.createErrorResponse("Invalid credentials");
            }

            String token = jwtService.generateToken(user);
            LOGGER.info("Login successful for user: {}", user.getUsername());

            return new LoginResponse(
                    true,
                    token,
                    "Login successful",
                    user.getType(),
                    user.getUsername()
            );
        } catch (Exception e) {
            LOGGER.error("Login error: {}", e.getMessage());
            return LoginResponse.createErrorResponse("Login failed. Please check your credentials.");
        }
    }

    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setType(request.getUserType());
            user.setCreatedAt(LocalDateTime.now());
            user.setEmailUpdates(false); // Set default value for emailUpdates

            if ("ngo".equalsIgnoreCase(request.getUserType())) {
                user.setOrganization(request.getOrganization());
                user.setArea(request.getArea());
            } else if ("donor".equalsIgnoreCase(request.getUserType())) {
                if (request.getOrganization() != null || request.getArea() != null) {
                    throw new IllegalArgumentException("Donor accounts cannot have organization or area fields.");
                }
            }

            userRepository.save(user);
            LOGGER.info("User registered successfully: {}", user.getUsername());
        } catch (Exception e) {
            LOGGER.error("Registration error: {}", e.getMessage());
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getEmail() == null || request.getPhone() == null) {
            throw new IllegalArgumentException("All fields (username, password, email, phone) are required.");
        }

        if (!request.getPhone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit phone number.");
        }

        if (!"ngo".equalsIgnoreCase(request.getUserType()) && !"donor".equalsIgnoreCase(request.getUserType())) {
            throw new IllegalArgumentException("Invalid account type. Only 'ngo' or 'donor' are allowed.");
        }

        if ("ngo".equalsIgnoreCase(request.getUserType())) {
            if (request.getOrganization() == null || request.getArea() == null) {
                throw new IllegalArgumentException("Organization and area are required for NGO accounts.");
            }
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists.");
        }
    }

    public List<UserDTO> getNGOs() {
        return userRepository.findByType("ngo")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setType(user.getType());
        dto.setOrganization(user.getOrganization());
        dto.setArea(user.getArea());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public ResponseEntity<?> getUserProfile(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("type", user.getType());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("organization", user.getOrganization());
            userData.put("area", user.getArea());
            userData.put("enabled", user.isEnabled());
            userData.put("accountNonLocked", user.isAccountNonLocked());
            userData.put("credentialsNonExpired", user.isCredentialsNonExpired());
            userData.put("accountNonExpired", user.isAccountNonExpired());

            List<Map<String, String>> authorities = user.getAuthorities().stream()
                    .map(auth -> Map.of("authority", auth.getAuthority()))
                    .collect(Collectors.toList());
            userData.put("authorities", authorities);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User profile fetched successfully",
                    "data", userData
            ));
        } catch (Exception e) {
            LOGGER.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error fetching user profile: " + e.getMessage()
            ));
        }
    }

    public ResponseEntity<?> changePassword(Long userId, ChangePasswordRequest request) {
        try {
            if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Current password and new password are required"));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                LOGGER.warn("Invalid current password for user: {}", user.getUsername());
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Current password is incorrect"));
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            LOGGER.info("Password changed successfully for user: {}", user.getUsername());
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));
        } catch (Exception e) {
            LOGGER.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error, please try again"));
        }
    }
}
