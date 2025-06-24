package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.domain.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    // Find user preferences by the associated User object
    Optional<UserPreference> findByUser(User user);
}