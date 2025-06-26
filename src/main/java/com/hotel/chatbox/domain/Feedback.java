package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true) // Assuming feedback might be without a specific room, or for a general hotel area
    private Room room; // You need to have a 'Room' entity defined elsewhere

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = true)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stay_id", nullable = true)
    private Stay stay;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;

    @Column(nullable = true)
    private String agentNotes;

    public enum FeedbackType {
        COMPLAINT, SUGGESTION, PRAISE, GENERAL
    }

    public enum FeedbackStatus {
        NEW, IN_REVIEW, RESOLVED, ESCALATED
    }

    public Feedback() {
        super();
    }

    // Updated constructor to remove 'scheduledTime' parameter
    public Feedback(Long feedbackId, User user, Room room, Hotel hotel, Stay stay, FeedbackType type, String message,
                    LocalDateTime submissionDate, FeedbackStatus status, String agentNotes) {
        super();
        this.feedbackId = feedbackId;
        this.user = user;
        this.room = room;
        this.hotel = hotel;
        this.stay = stay;
        this.type = type;
        this.message = message;
        this.submissionDate = submissionDate;
        this.status = status; 
        this.agentNotes = agentNotes;
    }

    // Getters and Setters

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Stay getStay() {
        return stay;
    }

    public void setStay(Stay stay) {
        this.stay = stay;
    }

    public FeedbackType getType() {
        return type;
    }

    public void setType(FeedbackType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    // REMOVED: Getter and Setter for ScheduledTime
    // public LocalDateTime getScheduledTime() {
    //     return scheduledTime;
    // }
    // public void setScheduledTime(LocalDateTime scheduledTime) {
    //     this.scheduledTime = scheduledTime;
    // }

    public FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }

    public String getAgentNotes() {
        return agentNotes;
    }

    public void setAgentNotes(String agentNotes) {
        this.agentNotes = agentNotes;
    }
}