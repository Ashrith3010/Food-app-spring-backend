package com.food_api.food_api.controller;

import com.food_api.food_api.dto.ChangePasswordRequest;
import com.food_api.food_api.dto.credentials.ProfileDTO;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.ApiResponse;
import com.food_api.food_api.service.UserService;
import com.food_api.food_api.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;


    // Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ProfileDTO profileDTO = new ProfileDTO(user);
        return ResponseEntity.ok(new ApiResponse(true, "User profile fetched successfully", profileDTO));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateUserProfile(@RequestBody ProfileDTO updatedProfile) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        // Validate unique fields
        if (!existingUser.getUsername().equals(updatedProfile.getUsername()) &&
                userRepository.existsByUsername(updatedProfile.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username already in use"));
        }

        if (!existingUser.getEmail().equals(updatedProfile.getEmail()) &&
                userRepository.existsByEmail(updatedProfile.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email already in use"));
        }

        if (!existingUser.getPhone().equals(updatedProfile.getPhone()) &&
                userRepository.existsByPhone(updatedProfile.getPhone())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Phone number already in use"));
        }

        // Update fields
        existingUser.setUsername(updatedProfile.getUsername());
        existingUser.setEmail(updatedProfile.getEmail());
        existingUser.setPhone(updatedProfile.getPhone());
        existingUser.setOrganization(updatedProfile.getOrganization());
        existingUser.setArea(updatedProfile.getArea());
        existingUser.setEmailUpdates(updatedProfile.getEmailUpdates());

        userRepository.save(existingUser);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request) {
        User user = (User) userDetails;
        return userService.changePassword(user.getId(), request);
    }
}
