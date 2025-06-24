package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.repository.BookingRepository;
import com.hotel.chatbox.repository.HotelRepository;
import com.hotel.chatbox.repository.HotelRoomRepository;
import com.hotel.chatbox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final HotelRoomRepository hotelRoomRepository;

    // Constructor Injection
    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            HotelRepository hotelRepository,
            HotelRoomRepository hotelRoomRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.hotelRoomRepository = hotelRoomRepository;
    }

    @Transactional
    public Booking createBooking(
            String username,
            String hotelName,
            String roomType, // This assumes roomType is a String directly in HotelRoom
            int numberOfGuests,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numberOfRooms // Typically 1 for a single booking request, but can be multiple
    ) {
        // 1. Fetch the User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        // 2. Fetch the Hotel
        Hotel hotel = hotelRepository.findByHotelName(hotelName)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found with name: " + hotelName));

        // 3. Fetch the specific HotelRoom by hotel and room type
        // Assuming HotelRoom has a 'roomType' (String) and a ManyToOne relationship to 'Hotel'
        // And assuming `roomType` on `HotelRoom` is actually storing the name (e.g., "Standard", "Deluxe")
        Optional<HotelRoom> optionalHotelRoom = hotelRoomRepository.findByHotelAndTypeName(hotel, roomType);

        if (optionalHotelRoom.isEmpty()) {
            throw new IllegalArgumentException("Room type '" + roomType + "' not available at hotel '" + hotelName + "'");
        }
        HotelRoom hotelRoom = optionalHotelRoom.get();


        // TODO: Implement actual room availability check before confirming the booking
        // You would typically query existing bookings for the chosen hotelRoom and dates
        // to ensure there are enough rooms available. For now, we proceed assuming availability.

        // 4. Create a new Booking instance
        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setHotelRoom(hotelRoom);
        newBooking.setCheckInDate(checkInDate);
        newBooking.setCheckOutDate(checkOutDate);
        newBooking.setNumberOfRoomsBooked(numberOfRooms);
        newBooking.setNumberOfGuests(numberOfGuests);
        newBooking.setStatus(com.hotel.chatbox.domain.Booking.BookingStatus.CONFIRMED); // Set initial status as CONFIRMED for this example

        // 5. Calculate Total Price
        double totalPrice = calculateBookingPrice(hotelRoom.getPrice(), checkInDate, checkOutDate, numberOfRooms);
        newBooking.setTotalPrice(totalPrice);

        // 6. Save the Booking
        return bookingRepository.save(newBooking);
    }

    /**
     * Calculates the total price for a booking.
     * You should refine this based on your actual pricing model (e.g., taxes, discounts).
     */
    private double calculateBookingPrice(double pricePerNight, LocalDate checkIn, LocalDate checkOut, int numberOfRooms) {
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return pricePerNight * nights * numberOfRooms;
    }

    /**
     * Retrieves a booking by its ID and the associated username.
     *
     * @param username The username of the user who made the booking.
     * @param bookingId The ID of the booking to retrieve.
     * @return An Optional containing the Booking if found, or empty if not found.
     * @throws IllegalArgumentException if the user is not found.
     */
    public Optional<Booking> getBookingByUsernameAndBookingId(String username, Long bookingId) {
        // 1. Fetch the User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        // 2. Find the booking by its ID and the fetched user
        return bookingRepository.findByBookingIdAndUser(bookingId, user);
    }
}