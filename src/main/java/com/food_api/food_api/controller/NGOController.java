package com.food_api.food_api.controller;


import com.food_api.food_api.dto.UserDTO;
import com.food_api.food_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/ngos")
public class NGOController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getNGODirectory() {
        try {
            List<UserDTO> ngos = userService.getNGOs();
            return ResponseEntity.ok(ngos);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "success", false,
                            "message", "Server error"
                    ));
        }
    }
}
