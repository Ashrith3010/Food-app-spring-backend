package com.food_api.food_api.controller;

import com.food_api.food_api.dto.ActivityDTO;
import com.food_api.food_api.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    @Autowired
    private ActivityService activityService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivity() {
        try {
            List<ActivityDTO> activities = activityService.getRecentActivity();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "activities", activities
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to fetch recent activity",
                    "error", e.getMessage()
            ));
        }
    }
}