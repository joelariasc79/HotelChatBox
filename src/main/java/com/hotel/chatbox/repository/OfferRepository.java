package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Find an offer by its unique code
    Optional<Offer> findByOfferCode(String offerCode);

    // Find all active offers
    List<Offer> findByIsActive(boolean isActive);

    // Find offers valid today
    List<Offer> findByValidFromBeforeAndValidToAfterAndIsActive(LocalDate today1, LocalDate today2, boolean isActive);

    // Find offers targeting a specific loyalty tier
    List<Offer> findByTargetLoyaltyTierAndIsActive(String loyaltyTier, boolean isActive);

    // Find offers based on discount type
    List<Offer> findByDiscountType(Offer.DiscountType discountType);

    // NEW: Find offers requiring a minimum number of bookings and are active
    List<Offer> findByMinBookingsRequiredLessThanEqualAndIsActive(Integer minBookingsRequired, boolean isActive);

    // NEW: Find offers requiring a minimum booking amount and are active
    List<Offer> findByMinBookingAmountLessThanEqualAndIsActive(Double minBookingAmount, boolean isActive);

    // Optional: Combine active, valid and target criteria
    List<Offer> findByIsActiveAndValidFromBeforeAndValidToAfterAndTargetLoyaltyTier(
            boolean isActive, LocalDate today1, LocalDate today2, String targetLoyaltyTier);

    List<Offer> findByIsActiveAndValidFromBeforeAndValidToAfterAndMinBookingsRequiredLessThanEqual(
            boolean isActive, LocalDate today1, LocalDate today2, Integer minBookingsRequired);

    List<Offer> findByIsActiveAndValidFromBeforeAndValidToAfterAndMinBookingAmountLessThanEqual(
            boolean isActive, LocalDate today1, LocalDate today2, Double minBookingAmount);

}