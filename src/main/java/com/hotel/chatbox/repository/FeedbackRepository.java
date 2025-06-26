package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Feedback;
import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Find all feedback submitted by a specific user
    List<Feedback> findByUser(User user);

    // Find all feedback for a specific hotel
    List<Feedback> findByHotel(Hotel hotel);

    // Find feedback by its type (e.g., COMPLAINT, SUGGESTION)
    List<Feedback> findByType(Feedback.FeedbackType type);

    // Find feedback by its status (e.g., NEW, RESOLVED, ESCALATED)
    List<Feedback> findByStatus(Feedback.FeedbackStatus status);

    // Find feedback by user and type
    List<Feedback> findByUserAndType(User user, Feedback.FeedbackType type);

    // Find feedback by user and status
    List<Feedback> findByUserAndStatus(User user, Feedback.FeedbackStatus status);
    
    
    List<Feedback> findByRoom_RoomId(Long roomId);
    
    
}