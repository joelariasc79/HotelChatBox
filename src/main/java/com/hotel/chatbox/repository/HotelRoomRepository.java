package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.RoomType; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRoomRepository extends JpaRepository<HotelRoom, Integer> {
    List<HotelRoom> findByHotelHotelId(int hotelId);
    
    /**
     * Finds a HotelRoom by its associated Hotel and RoomType.
     * This method is crucial for ensuring idempotence when creating HotelRoom entities,
     * allowing you to check if a specific room type for a given hotel already exists.
     *
     * @param hotel The Hotel entity.
     * @param type The RoomType entity.
     * @return An Optional containing the HotelRoom if found, or an empty Optional otherwise.
     */
    Optional<HotelRoom> findByHotelAndType(Hotel hotel, RoomType type); 
    List<HotelRoom> findByHotel(Hotel hotel);
    
//    Optional<HotelRoom> findByHotelAnd_Type(Hotel hotel, String roomType);   
    
    Optional<HotelRoom> findByHotelAndTypeName(Hotel hotel, String roomTypeName);
}