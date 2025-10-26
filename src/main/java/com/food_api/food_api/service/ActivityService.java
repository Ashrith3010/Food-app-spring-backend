package com.food_api.food_api.service;
import com.food_api.food_api.dto.ActivityDTO;
import com.food_api.food_api.entity.Donation;
import com.food_api.food_api.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {
    @Autowired
    private DonationRepository donationRepository;

    private static final int ACTIVITY_LIMIT = 3;

    public List<ActivityDTO> getRecentActivity() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        List<Donation> recentDonations = donationRepository.findByCreatedAtAfterOrClaimedAtAfter(
                cutoffTime, cutoffTime);

        List<ActivityDTO> activities = new ArrayList<>();

        for (Donation donation : recentDonations) {
            // Add donation creation activity
            if (donation.getCreatedAt().isAfter(cutoffTime)) {
                activities.add(new ActivityDTO(
                        "donation",
                        String.format("New donation of %s %s from %s",
                                donation.getQuantity(),
                                donation.getFoodItem(),
                                donation.getDonor().getUsername()),
                        donation.getCreatedAt(),
                        donation.getDonor().getUsername(),
                        donation.getDonor().getType()
                ));
            }

            // Add claim activity
            if (donation.getClaimedAt() != null && donation.getClaimedAt().isAfter(cutoffTime)) {
                activities.add(new ActivityDTO(
                        "pickup",
                        String.format("Food pickup claimed by %s",
                                donation.getClaimedBy().getUsername()),
                        donation.getClaimedAt(),
                        donation.getClaimedBy().getUsername(),
                        donation.getClaimedBy().getType()
                ));
            }
        }

        // Sort by timestamp descending and limit to 3 most recent
        return activities.stream()
                .sorted(Comparator.comparing(ActivityDTO::getTimestamp).reversed())
                .limit(ACTIVITY_LIMIT)
                .collect(Collectors.toList());
    }
}