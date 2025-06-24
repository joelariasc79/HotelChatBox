// src/main/java/com/hotel/chatbox/domain/Stay.java
package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stays")
public class Stay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false) // Links to the specific physical room
    private Room room;

    @Column(nullable = false)
    private LocalDateTime checkInActual;

    @Column(nullable = true) // Can be null until checkout
    private LocalDateTime checkOutActual;

    @Column(nullable = false)
    private double totalAmountPaid; // Final amount for this specific stay segment

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StayStatus stayStatus;

    @Column(nullable = false)
    private int loyaltyPointsEarned;

    public enum StayStatus {
        IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW, CHECKED_OUT
    }

    // Default constructor (important for JPA)
    public Stay() {
        // No-arg constructor
    }

    // Parameterized constructor (existing one, good for initial object creation in tests/data loading)
    public Stay(Long stayId, Booking booking, User user, Room room, LocalDateTime checkInActual,
            LocalDateTime checkOutActual, double totalAmountPaid, StayStatus stayStatus, int loyaltyPointsEarned) {
        this.stayId = stayId;
        this.booking = booking;
        this.user = user;
        this.room = room;
        this.checkInActual = checkInActual;
        this.checkOutActual = checkOutActual;
        this.totalAmountPaid = totalAmountPaid;
        this.stayStatus = stayStatus;
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }

    // --- Getters and Setters ---
    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getCheckInActual() {
        return checkInActual;
    }

    public void setCheckInActual(LocalDateTime checkInActual) {
        this.checkInActual = checkInActual;
    }

    public LocalDateTime getCheckOutActual() {
        return checkOutActual;
    }

    public void setCheckOutActual(LocalDateTime checkOutActual) {
        this.checkOutActual = checkOutActual;
    }

    public double getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(double totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public StayStatus getStayStatus() {
        return stayStatus;
    }

    public void setStayStatus(StayStatus stayStatus) {
        this.stayStatus = stayStatus;
    }

    public int getLoyaltyPointsEarned() {
        return loyaltyPointsEarned;
    }

    public void setLoyaltyPointsEarned(int loyaltyPointsEarned) {
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }
}