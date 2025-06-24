package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "room_reservations")
public class RoomReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true) // Link to specific room if assigned, otherwise room_type
    private HotelRoom hotelRoom; // Optional: Link to a specific room instance

    @Column(nullable = false)
    private String roomType; // e.g., "Standard", "Deluxe", "Suite"

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomReservationStatus status; // PENDING, CONFIRMED, CANCELLED

    // Enum for reservation status
    public enum RoomReservationStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    // --- Constructors ---
    public RoomReservation() {
        this.status = RoomReservationStatus.PENDING;
    }

    public RoomReservation(User user, String roomType, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        this();
        this.user = user;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        // Optionally set a specific hotelRoom here if logic assigns it immediately
    }

    // --- Getters and Setters ---
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HotelRoom getHotelRoom() {
        return hotelRoom;
    }

    public void setHotelRoom(HotelRoom hotelRoom) {
        this.hotelRoom = hotelRoom;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public RoomReservationStatus getStatus() {
        return status;
    }

    public void setStatus(RoomReservationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RoomReservation{" +
               "reservationId=" + reservationId +
               ", user=" + (user != null ? user.getUsername() : "null") +
               ", roomType='" + roomType + '\'' +
               ", checkInDate=" + checkInDate +
               ", checkOutDate=" + checkOutDate +
               ", numberOfGuests=" + numberOfGuests +
               ", status=" + status +
               '}';
    }
}