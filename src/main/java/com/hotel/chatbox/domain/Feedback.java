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
    @JoinColumn(name = "hotel_id", nullable = true)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stay_id", nullable = true) // Optional: Link to a specific stay
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

	public Feedback(Long feedbackId, User user, Hotel hotel, Stay stay, FeedbackType type, String message,
			LocalDateTime submissionDate, FeedbackStatus status, String agentNotes) {
		super();
		this.feedbackId = feedbackId;
		this.user = user;
		this.hotel = hotel;
		this.stay = stay;
		this.type = type;
		this.message = message;
		this.submissionDate = submissionDate;
		this.status = status;
		this.agentNotes = agentNotes;
	}

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



//package com.hotel.chatbox.domain;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime; // Using LocalDateTime for date and time
//
//@Entity
//@Table(name = "feedback")
//public class Feedback {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long feedbackId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "hotel_id", nullable = true) // Feedback can be general or for a specific hotel
//    private Hotel hotel;
//
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private FeedbackType type; // e.g., COMPLAINT, SUGGESTION, PRAISE
//
//    @Column(nullable = false, columnDefinition = "TEXT") // Use TEXT for potentially long messages
//    private String message;
//
//    @Column(nullable = false)
//    private LocalDateTime submissionDate;
//
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private FeedbackStatus status; // e.g., NEW, IN_REVIEW, RESOLVED, ESCALATED
//
//    @Column(nullable = true)
//    private String agentNotes; // Notes from the human agent
//
//    // Enum for feedback type
//    public enum FeedbackType {
//        COMPLAINT, SUGGESTION, PRAISE, GENERAL
//    }
//
//    // Enum for feedback status
//    public enum FeedbackStatus {
//        NEW, IN_REVIEW, RESOLVED, ESCALATED
//    }
//
//    // --- Constructors ---
//    public Feedback() {
//        this.submissionDate = LocalDateTime.now(); // Set submission date automatically
//        this.status = FeedbackStatus.NEW; // Default status
//    }
//
//    public Feedback(User user, Hotel hotel, FeedbackType type, String message) {
//        this(); // Call default constructor to set date and status
//        this.user = user;
//        this.hotel = hotel;
//        this.type = type;
//        this.message = message;
//    }
//
//    // --- Getters and Setters ---
//    public Long getFeedbackId() {
//        return feedbackId;
//    }
//
//    public void setFeedbackId(Long feedbackId) {
//        this.feedbackId = feedbackId;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public Hotel getHotel() {
//        return hotel;
//    }
//
//    public void setHotel(Hotel hotel) {
//        this.hotel = hotel;
//    }
//
//    public FeedbackType getType() {
//        return type;
//    }
//
//    public void setType(FeedbackType type) {
//        this.type = type;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public LocalDateTime getSubmissionDate() {
//        return submissionDate;
//    }
//
//    public void setSubmissionDate(LocalDateTime submissionDate) {
//        this.submissionDate = submissionDate;
//    }
//
//    public FeedbackStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(FeedbackStatus status) {
//        this.status = status;
//    }
//
//    public String getAgentNotes() {
//        return agentNotes;
//    }
//
//    public void setAgentNotes(String agentNotes) {
//        this.agentNotes = agentNotes;
//    }
//}