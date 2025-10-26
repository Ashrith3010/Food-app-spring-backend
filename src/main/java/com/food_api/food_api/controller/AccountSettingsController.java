package com.food_api.food_api.controller;

import com.food_api.food_api.dto.AccountSettingsDTO;
import com.food_api.food_api.entity.AccountSettings;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.AccountSettingsRepository;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account/settings")
@Slf4j
public class AccountSettingsController {

    private final AccountSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountSettingsController(
            AccountSettingsRepository settingsRepository,
            UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }
    private static final Logger log = LoggerFactory.getLogger(AccountSettingsController.class);

    // Add this debug endpoint to check authentication details
    @GetMapping("/debug")
    public ResponseEntity<?> debugAuthentication(Authentication authentication) {
        Map<String, Object> debugInfo = new HashMap<>();

        if (authentication == null) {
            debugInfo.put("error", "Authentication is null");
            return ResponseEntity.ok(debugInfo);
        }

        debugInfo.put("authentication_name", authentication.getName());
        debugInfo.put("authentication_type", authentication.getClass().getName());

        Object principal = authentication.getPrincipal();
        debugInfo.put("principal_type", principal.getClass().getName());

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            debugInfo.put("username", userDetails.getUsername());
            debugInfo.put("authorities", userDetails.getAuthorities());
        }

        // Check if user exists in database
        try {
            Optional<User> userByUsername = userRepository.findByUsername(authentication.getName());
            debugInfo.put("user_exists_by_username", userByUsername.isPresent());

            if (userByUsername.isPresent()) {
                User user = userByUsername.get();
                debugInfo.put("found_user_id", user.getId());
                debugInfo.put("found_user_email", user.getEmail());
                debugInfo.put("found_user_username", user.getUsername());
            }

            // List all users in database
            List<User> allUsers = userRepository.findAll();
            debugInfo.put("total_users_in_db", allUsers.size());
            debugInfo.put("all_usernames", allUsers.stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            debugInfo.put("error_checking_user", e.getMessage());
        }

        return ResponseEntity.ok(debugInfo);
    }

    @GetMapping
    public ResponseEntity<?> getSettings(Authentication authentication) {
        try {
            if (authentication == null) {
                log.error("Authentication is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Not authenticated", null));
            }

            String username = authentication.getName();
            log.info("Attempting to fetch settings for username: {}", username);

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                log.error("No user found with username: {}", username);
                // Try finding by email
                userOptional = userRepository.findByEmail(username);

                if (userOptional.isEmpty()) {
                    log.error("No user found with email: {}", username);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "User not found", null));
                }
            }

            User user = userOptional.get();
            log.info("Found user with ID: {} and username: {}", user.getId(), user.getUsername());

            AccountSettings settings = settingsRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        log.info("Creating default settings for user: {}", user.getId());
                        AccountSettings defaultSettings = new AccountSettings();
                        defaultSettings.setUser(user);
                        return settingsRepository.save(defaultSettings);
                    });

            AccountSettingsDTO dto = new AccountSettingsDTO();
            dto.setNotifications(settings.getNotifications());
            dto.setEmailUpdates(settings.getEmailUpdates());
            dto.setPrivacyMode(settings.getPrivacyMode());

            return ResponseEntity.ok(new ApiResponse(true, "Settings retrieved", dto));

        } catch (Exception e) {
            log.error("Error in getSettings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Server error: " + e.getMessage(), null));
        }

    }
    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody AccountSettingsDTO settingsDTO, Authentication authentication) {
        try {
            if (authentication == null) {
                log.error("Authentication is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Not authenticated", null));
            }

            String username = authentication.getName();
            log.info("Attempting to update settings for username: {}", username);

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                log.error("No user found with username: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User not found", null));
            }

            User user = userOptional.get();
            log.info("Found user with ID: {} and username: {}", user.getId(), user.getUsername());

            Optional<AccountSettings> settingsOptional = settingsRepository.findByUserId(user.getId());

            if (settingsOptional.isEmpty()) {
                log.error("Settings not found for user ID: {}. Creating default settings.", user.getId());
                AccountSettings newSettings = new AccountSettings();
                newSettings.setUser(user);
                settingsRepository.save(newSettings);
            }

            AccountSettings settings = settingsOptional.orElse(new AccountSettings());
            settings.setNotifications(settingsDTO.getNotifications());
            settings.setEmailUpdates(settingsDTO.getEmailUpdates());
            settings.setPrivacyMode(settingsDTO.getPrivacyMode());

            settingsRepository.save(settings);

            log.info("Settings updated successfully for user ID: {}", user.getId());

            return ResponseEntity.ok(new ApiResponse(true, "Settings updated successfully", null));

        } catch (Exception e) {
            log.error("Error in updateSettings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Server error: " + e.getMessage(), null));
        }
    }
}