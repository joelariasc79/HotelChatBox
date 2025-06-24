package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false) // Link to the specific physical room
    private Room room; // Renamed from hotelRoom to be explicit about physical room

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stay_id", nullable = true) // Optional: Link to the active stay
    private Stay stay;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime requestTime;
    @Column(nullable = true)
    private LocalDateTime scheduledTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public enum RequestType {
        ROOM_SERVICE, HOUSEKEEPING, MAINTENANCE, WAKE_UP_CALL, CONCIERGE, SPA, GYM, OTHER
    }

    public enum RequestStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

	public ServiceRequest() {
		super();
	}

	public ServiceRequest(Long requestId, User user, Room room, Stay stay, RequestType type, String description,
			LocalDateTime requestTime, LocalDateTime scheduledTime, RequestStatus status) {
		super();
		this.requestId = requestId;
		this.user = user;
		this.room = room;
		this.stay = stay;
		this.type = type;
		this.description = description;
		this.requestTime = requestTime;
		this.scheduledTime = scheduledTime;
		this.status = status;
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
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

	public Stay getStay() {
		return stay;
	}

	public void setStay(Stay stay) {
		this.stay = stay;
	}

	public RequestType getType() {
		return type;
	}

	public void setType(RequestType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(LocalDateTime requestTime) {
		this.requestTime = requestTime;
	}

	public LocalDateTime getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(LocalDateTime scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}
}