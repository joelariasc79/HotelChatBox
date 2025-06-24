package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    @Column(nullable = false, unique = true)
    private String offerCode;

    @Column(nullable = false)
    private String title; 

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discountValue;

    @Column(nullable = true)
    private LocalDate validFrom;

    @Column(nullable = true)
    private LocalDate validTo;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = true)
    private String targetLoyaltyTier;

    @Column(nullable = true)
    private Integer minBookingsRequired;
    @Column(nullable = true)
    private Double minBookingAmount;
    // New: Optional targeting for specific room types or amenities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_room_type_id", nullable = true)
    private RoomType targetRoomType; // If the offer applies to specific room types

    @Column(nullable = true)
    private String requiredAmenities; // Comma-separated list of amenity names for targeting

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

	public Offer() {
		super();
	}

	public Offer(Long offerId, String offerCode, String title, String description, DiscountType discountType,
			double discountValue, LocalDate validFrom, LocalDate validTo, boolean isActive, String targetLoyaltyTier,
			Integer minBookingsRequired, Double minBookingAmount, RoomType targetRoomType, String requiredAmenities) {
		super();
		this.offerId = offerId;
		this.offerCode = offerCode;
		this.title = title;
		this.description = description;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.isActive = isActive;
		this.targetLoyaltyTier = targetLoyaltyTier;
		this.minBookingsRequired = minBookingsRequired;
		this.minBookingAmount = minBookingAmount;
		this.targetRoomType = targetRoomType;
		this.requiredAmenities = requiredAmenities;
	}

	public Long getOfferId() {
		return offerId;
	}

	public void setOfferId(Long offerId) {
		this.offerId = offerId;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DiscountType getDiscountType() {
		return discountType;
	}

	public void setDiscountType(DiscountType discountType) {
		this.discountType = discountType;
	}

	public double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(double discountValue) {
		this.discountValue = discountValue;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getTargetLoyaltyTier() {
		return targetLoyaltyTier;
	}

	public void setTargetLoyaltyTier(String targetLoyaltyTier) {
		this.targetLoyaltyTier = targetLoyaltyTier;
	}

	public Integer getMinBookingsRequired() {
		return minBookingsRequired;
	}

	public void setMinBookingsRequired(Integer minBookingsRequired) {
		this.minBookingsRequired = minBookingsRequired;
	}

	public Double getMinBookingAmount() {
		return minBookingAmount;
	}

	public void setMinBookingAmount(Double minBookingAmount) {
		this.minBookingAmount = minBookingAmount;
	}

	public RoomType getTargetRoomType() {
		return targetRoomType;
	}

	public void setTargetRoomType(RoomType targetRoomType) {
		this.targetRoomType = targetRoomType;
	}

	public String getRequiredAmenities() {
		return requiredAmenities;
	}

	public void setRequiredAmenities(String requiredAmenities) {
		this.requiredAmenities = requiredAmenities;
	}
}


//package com.hotel.chatbox.domain;
//
//import jakarta.persistence.*;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "offers")
//public class Offer {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long offerId;
//
//    @Column(nullable = false, unique = true)
//    private String offerCode; // A unique code for the offer
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String description;
//
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private DiscountType discountType; // e.g., PERCENTAGE, FIXED_AMOUNT
//
//    @Column(nullable = false)
//    private double discountValue; // e.g., 10 (for 10%), 50 (for $50 off)
//
//    @Column(nullable = true)
//    private LocalDate validFrom;
//
//    @Column(nullable = true)
//    private LocalDate validTo;
//
//    @Column(nullable = false)
//    private boolean isActive; // Is the offer currently active
//
//    // Optional: Criteria for targeting
//    @Column(nullable = true)
//    private String targetLoyaltyTier; // e.g., "GOLD", "PLATINUM"
//    
//    @Column(nullable = true)
//    private Integer minBookingsRequired; // e.g., requires user to have made at least N bookings
//
//    @Column(nullable = true)
//    private Double minBookingAmount; // e.g., requires a minimum booking amount for the offer to apply
//
//    // Enum for discount type
//    public enum DiscountType {
//        PERCENTAGE, FIXED_AMOUNT
//    }
//
//    // --- Constructors ---
//    public Offer() {
//        this.isActive = true; // Default to active
//    }
//
//    public Offer(String offerCode, String title, String description, DiscountType discountType,
//                 double discountValue, LocalDate validFrom, LocalDate validTo) {
//        this();
//        this.offerCode = offerCode;
//        this.title = title;
//        this.description = description;
//        this.discountType = discountType;
//        this.discountValue = discountValue;
//        this.validFrom = validFrom;
//        this.validTo = validTo;
//    }
//
//    // --- Getters and Setters ---
//    public Long getOfferId() {
//        return offerId;
//    }
//
//    public void setOfferId(Long offerId) {
//        this.offerId = offerId;
//    }
//
//    public String getOfferCode() {
//        return offerCode;
//    }
//
//    public void setOfferCode(String offerCode) {
//        this.offerCode = offerCode;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public DiscountType getDiscountType() {
//        return discountType;
//    }
//
//    public void setDiscountType(DiscountType discountType) {
//        this.discountType = discountType;
//    }
//
//    public double getDiscountValue() {
//        return discountValue;
//    }
//
//    public void setDiscountValue(double discountValue) {
//        this.discountValue = discountValue;
//    }
//
//    public LocalDate getValidFrom() {
//        return validFrom;
//    }
//
//    public void setValidFrom(LocalDate validFrom) {
//        this.validFrom = validFrom;
//    }
//
//    public LocalDate getValidTo() {
//        return validTo;
//    }
//
//    public void setValidTo(LocalDate validTo) {
//        this.validTo = validTo;
//    }
//
//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean isActive) {
//        this.isActive = isActive;
//    }
//
//    public String getTargetLoyaltyTier() {
//        return targetLoyaltyTier;
//    }
//
//    public void setTargetLoyaltyTier(String targetLoyaltyTier) {
//        this.targetLoyaltyTier = targetLoyaltyTier;
//    }
//
//    public Integer getMinBookingsRequired() {
//        return minBookingsRequired;
//    }
//
//    public void setMinBookingsRequired(Integer minBookingsRequired) {
//        this.minBookingsRequired = minBookingsRequired;
//    }
//
//    public Double getMinBookingAmount() {
//        return minBookingAmount;
//    }
//
//    public void setMinBookingAmount(Double minBookingAmount) {
//        this.minBookingAmount = minBookingAmount;
//    }
//}