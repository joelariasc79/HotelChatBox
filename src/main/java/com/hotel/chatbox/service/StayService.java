// src/main/java/com/hotel/chatbox/service/StayService.java
package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.domain.Room; // Assuming you have a Room entity
import com.hotel.chatbox.repository.BookingRepository;
import com.hotel.chatbox.repository.StayRepository;
import com.hotel.chatbox.repository.UserRepository;
import com.hotel.chatbox.repository.RoomRepository; // Assuming you have a RoomRepository
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StayService {

    private final StayRepository stayRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository; // Inject RoomRepository

    // Constructor Injection
    public StayService(StayRepository stayRepository,
                       BookingRepository bookingRepository,
                       UserRepository userRepository,
                       RoomRepository roomRepository) { // Add RoomRepository here
        this.stayRepository = stayRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Creates and saves a new Stay record.
     * This method assumes you want to create a stay based on existing booking, user, and room IDs.
     * The `checkInActual` will be set to the current time, and `totalAmountPaid` will be initialized to 0.0.
     * The initial status will be IN_PROGRESS.
     *
     * @param bookingId The ID of the associated Booking.
     * @param userId The ID of the User for this stay.
     * @param roomId The ID of the specific physical Room for this stay.
     * @return The newly created and saved Stay object.
     * @throws IllegalArgumentException if Booking, User, or Room are not found.
     */
    @Transactional
    public Stay createStay(Long bookingId, Long userId, Long roomId) {
        // 1. Fetch the Booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // 2. Fetch the User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 3. Fetch the Room (physical room)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        // 4. Create the new Stay instance
        Stay newStay = new Stay();
        newStay.setBooking(booking);
        newStay.setUser(user);
        newStay.setRoom(room);
        newStay.setCheckInActual(LocalDateTime.now()); // Set actual check-in to now
        newStay.setStayStatus(Stay.StayStatus.IN_PROGRESS); // Initial status
        newStay.setTotalAmountPaid(0.0); // Initial amount (can be updated on checkout)
        newStay.setLoyaltyPointsEarned(0); // Initial points (can be updated on checkout)

        // 5. Save the Stay
        return stayRepository.save(newStay);
    }

    /**
     * Finds a Stay by its ID.
     * @param stayId The ID of the stay.
     * @return An Optional containing the Stay if found.
     */
    public Optional<Stay> getStayById(Long stayId) {
        return stayRepository.findById(stayId);
    }

	public Optional<Stay> findActiveStayForUserInRoom(User currentUser, Room room) {
		return stayRepository.findByUserAndRoomAndStayStatus(currentUser, room, Stay.StayStatus.IN_PROGRESS);
	}
	
	public List<Stay> findStaysByUserAndStayStatus(User user, Stay.StayStatus status) {
        return stayRepository.findByUserAndStayStatus(user, status);
    }

}


