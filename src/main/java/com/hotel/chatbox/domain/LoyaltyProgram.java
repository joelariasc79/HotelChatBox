package com.hotel.chatbox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_programs")
public class LoyaltyProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int programId;

    @Column(nullable = false, unique = true)
    private String tierName; // e.g., "BRONZE", "SILVER", "GOLD", "PLATINUM"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int pointsRequired; // Points needed to reach this tier

    @Column(nullable = false)
    private double discountPercentage; // Standard discount for this tier

    // --- Constructors ---
    public LoyaltyProgram() {
    }

    public LoyaltyProgram(String tierName, String description, int pointsRequired, double discountPercentage) {
        this.tierName = tierName;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.discountPercentage = discountPercentage;
    }

    // --- Getters and Setters ---
    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}