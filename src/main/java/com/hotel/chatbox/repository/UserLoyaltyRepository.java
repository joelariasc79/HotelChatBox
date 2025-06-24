package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.domain.UserLoyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserLoyaltyRepository extends JpaRepository<UserLoyalty, Long> {

    // Find a user's loyalty information by the associated User object
    Optional<UserLoyalty> findByUser(User user);

    // Find users within a specific loyalty tier
    List<UserLoyalty> findByCurrentTier_TierName(String tierName);

    // Find users with points greater than a certain amount
    List<UserLoyalty> findByCurrentPointsGreaterThan(int points);
}