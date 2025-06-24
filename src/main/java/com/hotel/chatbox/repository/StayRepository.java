package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByBooking(Booking booking);
    Optional<Stay> findByBookingAndRoom(Booking booking, Room room);
    List<Stay> findByRoom(Room room);

    // NEW: Find stays for a specific room with a given status (e.g., IN_PROGRESS)
    List<Stay> findByRoomAndStayStatus(Room room, Stay.StayStatus stayStatus);
    
    Optional<Stay> findByUserAndRoomAndStayStatus(User user, Room room, Stay.StayStatus status);
    
    List<Stay> findByUserAndStayStatus(User user, Stay.StayStatus status);

}
