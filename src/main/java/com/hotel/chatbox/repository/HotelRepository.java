package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    Optional<Hotel> findByHotelNameContainingIgnoreCase(String name);
    List<Hotel> findByCityContainingIgnoreCaseOrStateContainingIgnoreCaseOrAddressContainingIgnoreCase(String city, String state, String address);
    
    // Corrected query: Changed h.hotelRooms to h.hotelRoomTypes
    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.hotelRoomTypes WHERE h.hotelId = :hotelId")
    Optional<Hotel> findByIdWithRooms(@Param("hotelId") Integer hotelId);
       
//    Optional<Hotel> findByName(String name);// findByHotelName
    Optional<Hotel> findByHotelName(String name);// findByHotelName
}
