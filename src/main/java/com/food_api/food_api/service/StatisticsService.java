package com.food_api.food_api.service;

import com.food_api.food_api.entity.Donation;
import com.food_api.food_api.repository.DonationRepository;
import com.food_api.food_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Get all donations
        List<Donation> allDonations = donationRepository.findAll();

        // Filter active donations (not claimed AND not expired)
        List<Donation> activeDonations = allDonations.stream()
                .filter(donation ->
                        !donation.isClaimed() &&
                                donation.getExpiryTime().isAfter(now))
                .collect(Collectors.toList());

        // Count total donors
        stats.put("totalDonors", userRepository.countByType("donor"));

        // Count total NGOs
        stats.put("totalNGOs", userRepository.countByType("ngo"));

        // Count total donations (including expired)
        stats.put("totalDonations", (long) allDonations.size());

        // Count only active (non-expired, unclaimed) donations
        stats.put("activeDonations", (long) activeDonations.size());

        return stats;
    }
}