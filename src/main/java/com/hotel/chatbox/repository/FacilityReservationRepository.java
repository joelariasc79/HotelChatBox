package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.FacilityReservation;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacilityReservationRepository extends JpaRepository<FacilityReservation, Long> {

    // Find reservations by a specific user
    List<FacilityReservation> findByUser(User user);
    // Find reservations by facility type
    List<FacilityReservation> findByFacilityType(FacilityReservation.FacilityType facilityType);
    // Find reservations by date
    List<FacilityReservation> findByReservationDate(LocalDate reservationDate);
    // Find reservations by user and status
    List<FacilityReservation> findByUserAndStatus(User user, FacilityReservation.ReservationStatus status);
    // Find reservations by user and facility type
    List<FacilityReservation> findByUserAndFacilityType(User user, FacilityReservation.FacilityType facilityType);
}


//package com.hotel.chatbox.repository;
//
//import com.hotel.chatbox.domain.FacilityReservation;
//import com.hotel.chatbox.domain.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface FacilityReservationRepository extends JpaRepository<FacilityReservation, Long> {
//
//    // Find reservations by a specific user
//    List<FacilityReservation> findByUser(User user);
//
//    // Find reservations by facility type
//    List<FacilityReservation> findByFacilityType(FacilityReservation.FacilityType facilityType);
//
//    // Find reservations by date
//    List<FacilityReservation> findByReservationDate(LocalDate reservationDate);
//
//    // Find reservations by user and status
//    List<FacilityReservation> findByUserAndStatus(User user, FacilityReservation.ReservationStatus status);
//
//    // Find reservations by user and facility type
//    List<FacilityReservation> findByUserAndFacilityType(User user, FacilityReservation.FacilityType facilityType);
//}