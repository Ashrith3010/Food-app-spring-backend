
package com.food_api.food_api.controller;

import com.food_api.food_api.dto.credentials.LoginRequest;
import com.food_api.food_api.dto.credentials.LoginResponse;
import com.food_api.food_api.dto.RegisterRequest;
import com.food_api.food_api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            LOGGER.info("Login endpoint accessed by: {}", request.getUsername());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception e) {
            LOGGER.error("Login error for user: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(LoginResponse.createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            LOGGER.info("Registration successful for user: {}", request.getUsername());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LoginResponse(true, null, "Registration successful! Please login.", null, null));
        } catch (Exception e) {
            LOGGER.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(LoginResponse.createErrorResponse(e.getMessage()));
        }
    }
}
