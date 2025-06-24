package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a specific user
    List<Booking> findByUser(User user);

    // Find bookings for a specific hotel room within a date range
    List<Booking> findByHotelRoomAndCheckInDateBeforeAndCheckOutDateAfter(
            HotelRoom hotelRoom, LocalDate checkOutDate, LocalDate checkInDate);

    // Find bookings by user and status
    List<Booking> findByUserAndStatus(User user, Booking.BookingStatus status);

    // Find a booking by its ID and user
    Optional<Booking> findByBookingIdAndUser(Long bookingId, User user);
}