// DonationController.java
package com.food_api.food_api.controller;

import com.food_api.food_api.dto.DonationDTO;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    @Autowired
    private DonationService donationService;

    @PostMapping
    public ResponseEntity<?> createDonation(
            @RequestBody DonationDTO donationDTO,
            @AuthenticationPrincipal User currentUser) {

        if (!List.of("donor", "ngo").contains(currentUser.getType())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false,
                            "message", "Only donors and NGOs can create donations"));
        }

        try {
            DonationDTO createdDonation = donationService.createDonation(donationDTO, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Donation created successfully",
                    "donation", createdDonation
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false,
                            "message", "Server error",
                            "error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getDonations(
            @RequestParam(required = false) String viewType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal User currentUser) {

        try {
            List<DonationDTO> donations = donationService.getDonationsByFilters(
                    viewType, city, date, status, sortBy, currentUser);

            return ResponseEntity.ok(Map.of(
                    "donations", donations,
                    "metadata", Map.of("total", donations.size())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false,
                            "message", "Server error",
                            "error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getDonationsByUser(@AuthenticationPrincipal User currentUser) {
        try {
            System.out.println("===== Donation Controller Debug =====");
            System.out.println("Request received from user: " + currentUser.getUsername());
            System.out.println("User type: " + currentUser.getType());

            List<DonationDTO> donations = donationService.getDonationsByUser(currentUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "donations", donations,
                    "count", donations.size(),
                    "userType", currentUser.getType(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            System.err.println("Error in getDonationsByUser controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "success", false,
                            "message", "Server error",
                            "error", e.getMessage()
                    ));
        }
    }
    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claimDonation(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        try {
            DonationDTO claimedDonation = donationService.claimDonation(id, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Donation claimed successfully",
                    "updatedDonation", claimedDonation
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error"
            ));
        }
    }
}