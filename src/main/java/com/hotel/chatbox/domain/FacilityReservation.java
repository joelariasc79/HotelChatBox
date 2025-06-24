package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
// Using LocalTime for time

@Entity
@Table(name = "facility_reservations")
public class FacilityReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // Assuming you have a User entity

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;
    // e.g., ROOM_SERVICE, HOUSEKEEPING, SPA, GYM

    @Column(nullable = false)
    private LocalDate reservationDate;
    @Column(nullable = true) // Time might not be mandatory for all types (e.g., general housekeeping day)
    private LocalTime reservationTime;
    @Column(nullable = true, columnDefinition = "TEXT") // Details like room service items, spa service name
    private String details;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    // e.g., PENDING, CONFIRMED, COMPLETED, CANCELLED

    // Enum for facility type
    public enum FacilityType {
        ROOM_SERVICE, HOUSEKEEPING, SPA, GYM, OTHER
    }

    // Enum for reservation status
    public enum ReservationStatus {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }

    // --- Constructors ---
    public FacilityReservation() {
        this.status = ReservationStatus.PENDING;
        // Default status
    }

    public FacilityReservation(User user, FacilityType facilityType, LocalDate reservationDate, LocalTime reservationTime, String details) {
        this();
        // Call default constructor to set status
        this.user = user;
        this.facilityType = facilityType;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.details = details;
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

    public FacilityType getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(FacilityType facilityType) {
        this.facilityType = facilityType;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", facilityType=" + facilityType +
                ", reservationDate=" + reservationDate +
                ", reservationTime=" + reservationTime +
                ", details='" + details + '\'' +
                ", status=" + status +
                '}';
    }
}

//package com.hotel.chatbox.domain;
//
//import jakarta.persistence.*;
//import java.time.LocalDate;
//import java.time.LocalTime; // Using LocalTime for time
//
//@Entity
//@Table(name = "facility_reservations")
//public class FacilityReservation {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long reservationId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user; // Assuming you have a User entity
//
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private FacilityType facilityType; // e.g., ROOM_SERVICE, HOUSEKEEPING, SPA, GYM
//
//    @Column(nullable = false)
//    private LocalDate reservationDate;
//
//    @Column(nullable = true) // Time might not be mandatory for all types (e.g., general housekeeping day)
//    private LocalTime reservationTime;
//
//    @Column(nullable = true, columnDefinition = "TEXT") // Details like room service items, spa service name
//    private String details;
//
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private ReservationStatus status; // e.g., PENDING, CONFIRMED, COMPLETED, CANCELLED
//
//    // Enum for facility type
//    public enum FacilityType {
//        ROOM_SERVICE, HOUSEKEEPING, SPA, GYM, OTHER
//    }
//
//    // Enum for reservation status
//    public enum ReservationStatus {
//        PENDING, CONFIRMED, COMPLETED, CANCELLED
//    }
//
//    // --- Constructors ---
//    public FacilityReservation() {
//        this.status = ReservationStatus.PENDING; // Default status
//    }
//
//    public FacilityReservation(User user, FacilityType facilityType, LocalDate reservationDate, LocalTime reservationTime, String details) {
//        this(); // Call default constructor to set status
//        this.user = user;
//        this.facilityType = facilityType;
//        this.reservationDate = reservationDate;
//        this.reservationTime = reservationTime;
//        this.details = details;
//    }
//
//    // --- Getters and Setters ---
//    public Long getReservationId() {
//        return reservationId;
//    }
//
//    public void setReservationId(Long reservationId) {
//        this.reservationId = reservationId;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public FacilityType getFacilityType() {
//        return facilityType;
//    }
//
//    public void setFacilityType(FacilityType facilityType) {
//        this.facilityType = facilityType;
//    }
//
//    public LocalDate getReservationDate() {
//        return reservationDate;
//    }
//
//    public void setReservationDate(LocalDate reservationDate) {
//        this.reservationDate = reservationDate;
//    }
//
//    public LocalTime getReservationTime() {
//        return reservationTime;
//    }
//
//    public void setReservationTime(LocalTime reservationTime) {
//        this.reservationTime = reservationTime;
//    }
//
//    public String getDetails() {
//        return details;
//    }
//
//    public void setDetails(String details) {
//        this.details = details;
//    }
//
//    public ReservationStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(ReservationStatus status) {
//        this.status = status;
//    }
//
//    @Override
//    public String toString() {
//        return "Reservation{" +
//                "reservationId=" + reservationId +
//                ", user=" + (user != null ? user.getUsername() : "null") +
//                ", facilityType=" + facilityType +
//                ", reservationDate=" + reservationDate +
//                ", reservationTime=" + reservationTime +
//                ", details='" + details + '\'' +
//                ", status=" + status +
//                '}';
//    }
//}
//
