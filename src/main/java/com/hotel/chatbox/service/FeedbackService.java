package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.Feedback;
import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.repository.FeedbackRepository;
import com.hotel.chatbox.repository.RoomRepository;
import com.hotel.chatbox.repository.UserRepository;
import com.hotel.chatbox.repository.StayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
// Removed unused imports related to time formatting as scheduledTime is removed.
// import java.time.format.DateTimeFormatter;
// import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;
    private final FeedbackRepository feedbackRepository;

    // Constructor injection for repositories
    public FeedbackService(UserRepository userRepository, RoomRepository roomRepository, StayRepository stayRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.stayRepository = stayRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Creates and saves a new Feedback.
     *
     * @param userId. The ID of the user making the feedback.
     * @param roomId. The ID of the room for the feedback.
     * @param feedbackType The type of feedback (COMPLAINT, SUGGESTION, PRAISE, GENERAL).
     * @param description The detailed message/description of the feedback.
     * @param stayId Optional ID of the active stay associated with the feedback.
     * @return The saved Feedback entity.
     * @throws IllegalArgumentException if user or room is not found.
     */
 

    @Transactional
    public Feedback saveFeedback(Long userId, Long roomId, Feedback.FeedbackType type, String message, Long stayId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Room room = null;
        if (roomId != null) {
            room = roomRepository.findById(roomId).orElse(null);
            // Optionally: if room ID is provided but room is not found, you might want to throw an exception
            // .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));
        }

        Stay stay = null;
        Hotel hotel = null; // Initialize hotel as null

        if (stayId != null) {
            stay = stayRepository.findById(stayId).orElse(null);
            // If a stay is found, attempt to derive the hotel from it
            if (stay != null) {
                if (stay.getRoom() != null && stay.getRoom().getHotel() != null) {
                    hotel = stay.getRoom().getHotel();
                    // Optional: Log if hotel couldn't be derived despite stay existing
                    if (hotel == null) {
                        System.err.println("WARNING: Stay " + stayId + " found, but associated Hotel could not be derived from its Room/HotelRoom.");
                    }
                } else {
                    System.err.println("WARNING: Stay " + stayId + " found, but its associated Room or HotelRoom is null, cannot derive Hotel.");
                }
            } else {
                 System.err.println("WARNING: Stay not found with ID: " + stayId + ", so no hotel can be derived from stay.");
            }
        }

        Feedback feedback = new Feedback(
            null, // feedbackId (will be auto-generated)
            user,
            room, // Can be null
            hotel, // Can be null 
            stay, // Can be null
            type,
            message,
            LocalDateTime.now(),
            Feedback.FeedbackStatus.NEW,
            null // agentNotes
        );

        return feedbackRepository.save(feedback);
    }
    
//    @Transactional
//    public Feedback saveFeedback(Long userId, Long roomId,
//                                 Feedback.FeedbackType feedbackType,
//                                 String description,
//                                 // Removed: LocalDateTime scheduledTime,
//                                 Long stayId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));
//
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found."));
//
//        Stay stay = null;
//        if (stayId != null) {
//            stay = stayRepository.findById(stayId)
//                    .orElseThrow(() -> new IllegalArgumentException("Stay with ID " + stayId + " not found."));
//            // Optional: You might want to add a check here to ensure the stay is active and belongs to the user/room.
//            // Example: if (!stay.getUser().getId().equals(userId) || !stay.getRoom().getRoomId().equals(room.getRoomId())) { ... }
//        } else {
//            // If no stayId is provided, try to find an active stay for the user in that room
//            stay = stayRepository.findByUserAndRoomAndStayStatus(user, room, Stay.StayStatus.IN_PROGRESS)
//                        .orElse(null); // Assign null if no active stay is found
//            if (stay == null) {
//                System.out.println("No active stay found for user " + userId + " in room ID " + roomId + ". Feedback will be processed without a linked stay.");
//            }
//        }
//
//        Feedback feedback = new Feedback();
//        feedback.setUser(user);
//        feedback.setRoom(room);
//        feedback.setStay(stay);
//        feedback.setType(feedbackType);
//        feedback.setMessage(description);
//        feedback.setSubmissionDate(LocalDateTime.now());
//        // Removed: feedback.setScheduledTime(scheduledTime);
//        feedback.setStatus(Feedback.FeedbackStatus.NEW);
//
//        return feedbackRepository.save(feedback);
//    }

    /**
     * Creates and saves a new Feedback.
     *
     * @param userId. The ID of the user making the feedback.
     * @param roomId. The ID of the room for the feedback.
     * @param items The specific items or details for the feedback message.
     * @param stayId. Optional ID of the active stay associated with the request (can be null).
     * @return The saved Feedback entity.
     * @throws IllegalArgumentException if user or room is not found.
     */
    @Transactional
    public Feedback createFeedback(Long userId, Long roomId, String items, /* Removed: String scheduledTimeStr, */ Long stayId) {
        // Removed all LocalDateTime scheduledTime parsing logic as it's no longer needed for Feedback
        // LocalDateTime scheduledTime = null;
        // if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) { ... }

        String description = "Room Service: " + items; // This description might need adjustment if it's not specific to 'Room Service' feedback.

        // Call saveFeedback without the scheduledTime parameter
        return saveFeedback(userId, roomId, Feedback.FeedbackType.GENERAL, description, stayId);
    }

    /**
     * Retrieves a feedback by its ID.
     * @param feedbackId. The ID of the feedback.
     * @return An Optional containing the Feedback if found, empty otherwise.
     */
    public Optional<Feedback> getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId);
    }

    /**
     * Updates the status of a feedback.
     * @param feedbackId. The ID of the feedback to update.
     * @param newStatus. The new status.
     * @return The updated Feedback entity.
     * @throws IllegalArgumentException if the feedback is not found.
     */
    @Transactional
    public Feedback updateFeedbackStatus(Long feedbackId, Feedback.FeedbackStatus newStatus) { // Corrected feebackId to feedbackId
    	Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback with ID " + feedbackId + " not found."));
    	feedback.setStatus(newStatus);
        return feedbackRepository.save(feedback);
    }

    /**
     * Retrieves all feedbacks for a given user.
     * @param user. The user entity.
     * @return A list of feedbacks.
     */
    public List<Feedback> getFeedbackByUser(User user) {
        return feedbackRepository.findByUser(user);
    }

    /**
     * Retrieves all feedbacks for a given room.
     * @param roomId. The ID of the room.
     * @return A list of feedbacks.
     */
    public List<Feedback> getFeedbackByRoom(Long roomId) {
        return feedbackRepository.findByRoom_RoomId(roomId);
    }

    /**
     * Retrieves all feedbacks of a specific type.
     * @param type The feedback type.
     * @return A list of feedbacks.
     */
    public List<Feedback> getFeedbackByType(Feedback.FeedbackType type) {
        return feedbackRepository.findByType(type);
    }
}