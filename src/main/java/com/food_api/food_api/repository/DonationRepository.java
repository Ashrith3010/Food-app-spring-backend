package com.food_api.food_api.repository;

import com.food_api.food_api.entity.Donation;
import com.food_api.food_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByClaimedFalseAndExpiryTimeAfter(LocalDateTime now);
    List<Donation> findByClaimed(boolean claimed);
    List<Donation> findByLocationIgnoreCase(String location);
    long countByClaimed(boolean claimed);
    List<Donation> findByDonor(User donor);
    List<Donation> findByClaimedBy(User claimedBy);
    List<Donation> findByClaimedFalse();

    // Add these queries for debugging
    @Query("SELECT d FROM Donation d WHERE d.claimedBy.id = :userId")
    List<Donation> findByClaimedByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM Donation d WHERE d.claimed = true")
    List<Donation> findAllClaimedDonations();

    // New method for finding recent donations
    List<Donation> findByCreatedAtAfterOrClaimedAtAfter(LocalDateTime createdAfter, LocalDateTime claimedAfter);

    // Alternative method using @Query for more control
    @Query("SELECT d FROM Donation d WHERE d.createdAt > :timestamp OR " +
            "(d.claimedAt IS NOT NULL AND d.claimedAt > :timestamp)")
    List<Donation> findRecentActivity(@Param("timestamp") LocalDateTime timestamp);
}
