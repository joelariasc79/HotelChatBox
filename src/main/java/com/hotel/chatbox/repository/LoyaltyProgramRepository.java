package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.LoyaltyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Integer> {

    // Find a loyalty program tier by its name (e.g., "GOLD")
    Optional<LoyaltyProgram> findByTierName(String tierName);

    // Find all loyalty programs ordered by points required
    List<LoyaltyProgram> findAllByOrderByPointsRequiredAsc();
}