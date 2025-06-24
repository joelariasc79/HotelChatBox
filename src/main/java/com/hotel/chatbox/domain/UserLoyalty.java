package com.hotel.chatbox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_loyalty")
public class UserLoyalty {
    @Id
    private Long userId; // PK is also FK to User table

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int currentPoints;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER fetch often useful for loyalty tier
    @JoinColumn(name = "loyalty_program_id", nullable = false) // Links to the current tier
    private LoyaltyProgram currentTier;

    // --- Constructors ---
    public UserLoyalty() {
    }

    public UserLoyalty(User user, int currentPoints, LoyaltyProgram currentTier) {
        this.user = user;
        this.userId = user.getId();
        this.currentPoints = currentPoints;
        this.currentTier = currentTier;
    }

    // --- Getters and Setters ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public LoyaltyProgram getCurrentTier() {
        return currentTier;
    }

    public void setCurrentTier(LoyaltyProgram currentTier) {
        this.currentTier = currentTier;
    }
}