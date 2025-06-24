package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.RoomReservation;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomReservationRepository extends JpaRepository<RoomReservation, Long> {
    List<RoomReservation> findByUser(User user);
    List<RoomReservation> findByRoomType(String roomType);
    List<RoomReservation> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    List<RoomReservation> findByUserAndStatus(User user, RoomReservation.RoomReservationStatus status);
}