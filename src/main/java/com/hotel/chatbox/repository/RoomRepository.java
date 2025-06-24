package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.RoomType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // You can add custom query methods here if needed
    List<Room> findByHotelRoomTypeAndAvailabilityStatus(HotelRoom hotelRoomType, Room.RoomAvailabilityStatus availabilityStatus);
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
 // Find a physical room by its number within a specific hotel
    Optional<Room> findByHotelAndRoomNumber(Hotel hotel, String roomNumber);

    List<Room> findByHotel(Hotel hotel);

    // Add other methods as needed, e.g., findByAvailabilityStatus
    List<Room> findByAvailabilityStatus(Room.RoomAvailabilityStatus availabilityStatus);
    
}