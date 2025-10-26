// DonationService.java
package com.food_api.food_api.service;

import com.food_api.food_api.dto.DonationDTO;
import com.food_api.food_api.entity.Donation;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.DonationRepository;
import com.food_api.food_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DonationService {
    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public DonationDTO createDonation(DonationDTO donationDTO, User currentUser) {
        Donation donation = new Donation();
        donation.setFoodItem(donationDTO.getFoodItem());
        donation.setQuantity(donationDTO.getQuantity());
        donation.setLocation(donationDTO.getLocation());
        donation.setArea(donationDTO.getArea());
        donation.setExpiryTime(donationDTO.getExpiryTime());
        donation.setServingSize(donationDTO.getServingSize());
        donation.setStorageInstructions(donationDTO.getStorageInstructions());
        donation.setDietaryInfo(donationDTO.getDietaryInfo());
        donation.setDonor(currentUser);
        donation.setClaimed(false);
        donation.setCreatedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());

        Donation savedDonation = donationRepository.save(donation);
        // Add logging for email notifications
        System.out.println("Starting email notifications for new donation ID: " + savedDonation.getId());

        List<User> nearbyNGOs = userRepository.findByTypeAndArea("ngo", donation.getArea());
        System.out.println("Found " + nearbyNGOs.size() + " nearby NGOs in area: " + donation.getArea());

        int emailsSent = 0;
        for (User ngo : nearbyNGOs) {
            try {
                System.out.println("Attempting to send notification to NGO: " + ngo.getUsername() + " (Email: " + ngo.getEmail() + ")");
                emailService.sendDonationNotificationToNGOs(convertToDTO(savedDonation), ngo);
                emailsSent++;
                System.out.println("Successfully sent notification to NGO: " + ngo.getUsername());
            } catch (Exception e) {
                System.err.println("Failed to send notification to NGO: " + ngo.getUsername());
                System.err.println("Error details: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Email notification process completed. Successfully sent " + emailsSent + " out of " + nearbyNGOs.size() + " notifications");

        return convertToDTO(savedDonation);
    }


    public List<DonationDTO> getDonationsByFilters(String viewType, String city, LocalDateTime date,
                                                   String status, String sortBy, User currentUser) {
        List<Donation> donations = donationRepository.findAll();

        // Apply filters
        if (viewType != null) {
            donations = applyViewTypeFilter(donations, viewType, currentUser);
        }

        if (city != null) {
            donations = donations.stream()
                    .filter(d -> d.getLocation().equalsIgnoreCase(city) || d.getArea().equalsIgnoreCase(city))
                    .collect(Collectors.toList());
        }

        if (date != null) {
            donations = donations.stream()
                    .filter(d -> d.getCreatedAt().toLocalDate().equals(date.toLocalDate()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            donations = applyStatusFilter(donations, status);
        }

        if (sortBy != null) {
            donations = sortDonations(donations, sortBy);
        }

        return donations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    public DonationDTO claimDonation(String donationId, User claimingUser) {
        System.out.println("Processing donation claim - Donation ID: " + donationId + ", Claiming User: " + claimingUser.getUsername());

        Donation donation = donationRepository.findById(Long.parseLong(donationId))
                .orElseThrow(() -> new IllegalStateException("Donation not found"));

        if (donation.isClaimed()) {
            System.out.println("Claim failed - Donation " + donationId + " is already claimed");
            throw new IllegalStateException("Donation already claimed");
        }

        // Add validation for donor email
        if (donation.getDonor() == null || donation.getDonor().getEmail() == null || donation.getDonor().getEmail().trim().isEmpty()) {
            throw new IllegalStateException("Cannot claim donation: donor email is missing");
        }

        donation.setClaimed(true);
        donation.setClaimedBy(claimingUser);
        donation.setClaimedAt(LocalDateTime.now());

        Donation updatedDonation = donationRepository.save(donation);

        System.out.println("Donation " + donationId + " successfully claimed. Sending notification to donor: " +
                donation.getDonor().getUsername() + " (Email: " + donation.getDonor().getEmail() + ")");

        try {
            emailService.sendDonationClaimedNotification(convertToDTO(updatedDonation), claimingUser);
            System.out.println("Successfully sent claim notification to donor");
        } catch (Exception e) {
            System.err.println("Failed to send claim notification to donor");
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            // Consider whether you want to rollback the claim if email notification fails
            // For now, we'll just log the error but allow the claim to proceed
        }

        return convertToDTO(updatedDonation);
    }

    private List<Donation> applyViewTypeFilter(List<Donation> donations, String viewType, User currentUser) {
        switch (viewType) {
            case "available":
                return donations.stream()
                        .filter(d -> !d.isClaimed() && d.getExpiryTime().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "my-donations":
                if ("donor".equals(currentUser.getType())) {
                    return donations.stream()
                            .filter(d -> d.getDonor().getId().equals(currentUser.getId()))
                            .collect(Collectors.toList());
                }
                break;
            case "claimed":
                return donations.stream()
                        .filter(Donation::isClaimed)
                        .collect(Collectors.toList());
        }
        return donations;
    }

    private List<Donation> applyStatusFilter(List<Donation> donations, String status) {
        switch (status) {
            case "active":
                return donations.stream()
                        .filter(d -> !d.isClaimed() && d.getExpiryTime().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "expired":
                return donations.stream()
                        .filter(d -> d.getExpiryTime().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "claimed":
                return donations.stream()
                        .filter(Donation::isClaimed)
                        .collect(Collectors.toList());
        }
        return donations;
    }

    private List<Donation> sortDonations(List<Donation> donations, String sortBy) {
        switch (sortBy) {
            case "date":
                donations.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                break;
            case "expiry":
                donations.sort((a, b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
                break;
            case "quantity":
                donations.sort((a, b) -> Integer.compare(
                        Integer.parseInt(b.getQuantity()),
                        Integer.parseInt(a.getQuantity())
                ));
                break;
        }
        return donations;
    }
    public List<DonationDTO> getDonationsByUser(User user) {
        List<Donation> donations = new ArrayList<>();
        try {
            System.out.println("Getting donations for user: " + user.getId() + ", type: " + user.getType());

            if ("ngo".equals(user.getType())) {
                // For NGOs, get both their claimed donations AND donations they created
                List<Donation> claimedDonations = donationRepository.findByClaimedBy(user);
                List<Donation> createdDonations = donationRepository.findByDonor(user);
                donations.addAll(claimedDonations);
                donations.addAll(createdDonations);

                System.out.println("NGO donations - Claimed: " + claimedDonations.size() +
                        ", Created: " + createdDonations.size());
            } else if ("donor".equals(user.getType())) {
                donations = donationRepository.findByDonor(user);
                System.out.println("Donor donations found: " + donations.size());
            }

            List<DonationDTO> dtos = donations.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            System.out.println("Created DTOs: " + dtos.size());
            return dtos;

        } catch (Exception e) {
            System.err.println("Error in getDonationsByUser: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private DonationDTO convertToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        try {
            dto.setId(donation.getId().toString());
            dto.setFoodItem(donation.getFoodItem());
            dto.setQuantity(donation.getQuantity());
            dto.setLocation(donation.getLocation());
            dto.setArea(donation.getArea());
            dto.setExpiryTime(donation.getExpiryTime());
            dto.setServingSize(donation.getServingSize());
            dto.setStorageInstructions(donation.getStorageInstructions());
            dto.setDietaryInfo(donation.getDietaryInfo());

            if (donation.getDonor() != null) {
                dto.setDonorId(donation.getDonor().getId().toString());
                dto.setDonorName(donation.getDonor().getUsername());
                dto.setDonorEmail(donation.getDonor().getEmail());  // Add this line to set the donor email
                dto.setDonorType(donation.getDonor().getType());
            }

            dto.setClaimed(donation.isClaimed());

            if (donation.getClaimedBy() != null) {
                dto.setClaimedBy(donation.getClaimedBy().getId().toString());
            }

            dto.setClaimedAt(donation.getClaimedAt());
            dto.setCreatedAt(donation.getCreatedAt());
            dto.setUpdatedAt(donation.getUpdatedAt());

            System.out.println("Successfully converted donation ID " + donation.getId() + " to DTO" +
                    " (Donor Email: " + dto.getDonorEmail() + ")");  // Add logging for donor email
        } catch (Exception e) {
            System.err.println("Error converting donation ID " + donation.getId() + " to DTO: " + e.getMessage());
            e.printStackTrace();
        }
        return dto;
    }
}