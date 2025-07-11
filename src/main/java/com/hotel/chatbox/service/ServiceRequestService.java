// src/main/java/com/hotel/chatbox/service/ServiceRequestService.java
package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.ServiceRequest;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.repository.ServiceRequestRepository;
import com.hotel.chatbox.repository.RoomRepository; // Keep this import to fetch Room by ID
import com.hotel.chatbox.repository.UserRepository;
import com.hotel.chatbox.repository.StayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Added for parsing
import java.time.format.DateTimeParseException; // Added for parsing
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository; // Still needed to fetch Room object by ID
    private final StayRepository stayRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 UserRepository userRepository,
                                 RoomRepository roomRepository, // Keep this in constructor
                                 StayRepository stayRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository; // Initialize it
        this.stayRepository = stayRepository;
    }

    /**
     * Creates and saves a new service request.
     *
     * @param userId The ID of the user making the request.
     * @param roomId The ID of the room for which the service is requested.
     * @param requestType The type of service request (e.g., ROOM_SERVICE, HOUSEKEEPING).
     * @param description A detailed description of the request.
     * @param scheduledTime Optional time for the service to be scheduled (can be null for immediate).
     * @param stayId Optional ID of the active stay associated with the request (can be null).
     * @return The saved ServiceRequest entity.
     * @throws IllegalArgumentException if user or room is not found.
     */
    @Transactional
    public ServiceRequest createServiceRequest(Long userId, Long roomId, 
                                               ServiceRequest.RequestType requestType,
                                               String description,
                                               LocalDateTime scheduledTime,
                                               Long stayId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        // Fetch Room by roomId
        Room room = roomRepository.findById(roomId) // Changed from findByRoomNumber
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found."));

        Stay stay = null;
        if (stayId != null) {
            stay = stayRepository.findById(stayId)
                    .orElseThrow(() -> new IllegalArgumentException("Stay with ID " + stayId + " not found."));
            // Optional: You might want to add a check here to ensure the stay is active and belongs to the user/room.
            // Example: if (!stay.getUser().getId().equals(userId) || !stay.getRoom().getRoomId().equals(room.getRoomId())) { ... }
        } else {
            // If no stayId is provided, try to find an active stay for the user in that room
            // Note: This logic might be redundant if ChatService always provides stayId/roomId based on active stay
            stay = stayRepository.findByUserAndRoomAndStayStatus(user, room, Stay.StayStatus.IN_PROGRESS)
                        .orElse(null); // Assign null if no active stay is found
            if (stay == null) {
                System.out.println("No active stay found for user " + userId + " in room ID " + roomId + ". Request will be processed without a linked stay.");
            }
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setUser(user);
        serviceRequest.setRoom(room);
        serviceRequest.setStay(stay); 
        serviceRequest.setType(requestType);
        serviceRequest.setDescription(description);
        serviceRequest.setRequestTime(LocalDateTime.now());
        serviceRequest.setScheduledTime(scheduledTime); 
        serviceRequest.setStatus(ServiceRequest.RequestStatus.PENDING);

        return serviceRequestRepository.save(serviceRequest);
    }

    /**
     * Creates and saves a new Room Service request.
     *
     * @param userId The ID of the user making the request.
     * @param roomId The ID of the room for which the service is requested. // Changed from roomNumber
     * @param items The specific items requested for room service.
     * @param scheduledTimeStr The scheduled time for the service in HH:MM format.
     * @param stayId Optional ID of the active stay associated with the request (can be null).
     * @return The saved ServiceRequest entity.
     * @throws IllegalArgumentException if user or room is not found, or time format is invalid.
     */
    @Transactional
    public ServiceRequest createRoomServiceRequest(Long userId, Long roomId, String items, String scheduledTimeStr, Long stayId) {
        LocalDateTime scheduledTime = null;

        if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) {
            try {
                // Attempt 1: Try parsing the full LocalDateTime format (YYYY-MM-DDTHH:MM)
                scheduledTime = LocalDateTime.parse(scheduledTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e1) {
                try {
                    // Attempt 2: If full date-time fails, try parsing HH:MM and combine with current date
                    scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("HH:mm")));
                } catch (DateTimeParseException e2) {
                    try {
                        // Attempt 3: If HH:MM fails, try parsing h:mm a (e.g., 7:30 PM) and combine with current date
                        scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("h:mm a")));
                    } catch (DateTimeParseException e3) {
                        // If all parsing attempts fail, throw an IllegalArgumentException
                        throw new IllegalArgumentException(
                            "Invalid time format for scheduled service. Please use 'YYYY-MM-DDTHH:MM' (e.g., '2025-06-26T19:00'), 'HH:MM' (e.g., '19:30'), or 'H:MM AM/PM' (e.g., '7:30 PM').", e3
                        );
                    }
                }
            }
        }

        String description = "Room Service: " + items;

        return createServiceRequest(userId, roomId, ServiceRequest.RequestType.ROOM_SERVICE, description, scheduledTime, stayId);
    }
    
    @Transactional
    public ServiceRequest scheduleHousekeeping(Long userId, Long roomId, String scheduledTimeStr, Long stayId) {
        LocalDateTime scheduledTime = null;
        if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) {
            try {
                // Attempt 1: Try parsing the full LocalDateTime format (YYYY-MM-DDTHH:MM)
                scheduledTime = LocalDateTime.parse(scheduledTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e1) {
                try {
                    // Attempt 2: If full date-time fails, try parsing HH:MM and combine with current date
                    scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("HH:mm")));
                } catch (DateTimeParseException e2) {
                    try {
                        // Attempt 3: If HH:MM fails, try parsing h:mm a (e.g., 7:30 PM) and combine with current date
                        scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("h:mm a")));
                    } catch (DateTimeParseException e3) {
                        // If all parsing attempts fail, throw an IllegalArgumentException
                        throw new IllegalArgumentException(
                            "Invalid time format for scheduled service. Please use 'YYYY-MM-DDTHH:MM' (e.g., '2025-06-26T19:00'), 'HH:MM' (e.g., '19:30'), or 'H:MM AM/PM' (e.g., '7:30 PM').", e3
                        );
                    }
                }
            }
        }

        String description = "Housekeeping Service";

        return createServiceRequest(userId, roomId, ServiceRequest.RequestType.HOUSEKEEPING, description, scheduledTime, stayId);
    }


    /**
     * Retrieves a service request by its ID.
     * @param requestId The ID of the service request.
     * @return An Optional containing the ServiceRequest if found, empty otherwise.
     */
    public Optional<ServiceRequest> getServiceRequestById(Long requestId) {
        return serviceRequestRepository.findById(requestId);
    }

    /**
     * Updates the status of a service request.
     * @param requestId The ID of the service request to update.
     * @param newStatus The new status.
     * @return The updated ServiceRequest entity.
     * @throws IllegalArgumentException if the request is not found.
     */
    @Transactional
    public ServiceRequest updateServiceRequestStatus(Long requestId, ServiceRequest.RequestStatus newStatus) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service Request with ID " + requestId + " not found."));
        serviceRequest.setStatus(newStatus);
        return serviceRequestRepository.save(serviceRequest);
    }

    /**
     * Retrieves all service requests for a given user.
     * @param user The user entity.
     * @return A list of service requests.
     */
    public List<ServiceRequest> getServiceRequestsByUser(User user) {
        return serviceRequestRepository.findByUser(user);
    }

    /**
     * Retrieves all service requests for a given room.
     * @param roomId The ID of the room. // Changed from Room room
     * @return A list of service requests.
     */
    public List<ServiceRequest> getServiceRequestsByRoom(Long roomId) { 
        return serviceRequestRepository.findByRoom_RoomId(roomId);
    }

    /**
     * Retrieves all service requests of a specific type.
     * @param type The request type.
     * @return A list of service requests.
     */
    public List<ServiceRequest> getServiceRequestsByType(ServiceRequest.RequestType type) {
        return serviceRequestRepository.findByType(type);
    }
}