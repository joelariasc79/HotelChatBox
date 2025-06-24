package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenitiesRepository extends JpaRepository<Amenities, Integer> {
    Optional<Amenities> findByName(String name);
}