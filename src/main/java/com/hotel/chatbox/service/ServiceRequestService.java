// src/main/java/com/hotel/chatbox/service/ServiceRequestService.java
package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.ServiceRequest;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.repository.ServiceRequestRepository;
import com.hotel.chatbox.repository.RoomRepository;
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
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 UserRepository userRepository,
                                 RoomRepository roomRepository,
                                 StayRepository stayRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.stayRepository = stayRepository;
    }

    /**
     * Creates and saves a new service request.
     *
     * @param userId The ID of the user making the request.
     * @param roomNumber The room number for which the service is requested.
     * @param requestType The type of service request (e.g., ROOM_SERVICE, HOUSEKEEPING).
     * @param description A detailed description of the request.
     * @param scheduledTime Optional time for the service to be scheduled (can be null for immediate).
     * @param stayId Optional ID of the active stay associated with the request (can be null).
     * @return The saved ServiceRequest entity.
     * @throws IllegalArgumentException if user or room is not found.
     */
    @Transactional
    public ServiceRequest createServiceRequest(Long userId, String roomNumber,
                                               ServiceRequest.RequestType requestType,
                                               String description,
                                               LocalDateTime scheduledTime,
                                               Long stayId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room with number " + roomNumber + " not found."));

        Stay stay = null;
        if (stayId != null) {
            stay = stayRepository.findById(stayId)
                    .orElseThrow(() -> new IllegalArgumentException("Stay with ID " + stayId + " not found."));
            // Optional: You might want to add a check here to ensure the stay is active and belongs to the user/room.
            // Example: if (!stay.getUser().getId().equals(userId) || !stay.getRoom().getRoomId().equals(room.getRoomId())) { ... }
        } else {
             // If no stayId is provided, try to find an active stay for the user in that room
             stay = stayRepository.findByUserAndRoomAndStayStatus(user, room, Stay.StayStatus.IN_PROGRESS)
                         .orElse(null); // Assign null if no active stay is found
             if (stay == null) {
                 System.out.println("No active stay found for user " + userId + " in room " + roomNumber + ". Request will be processed without a linked stay.");
             }
        }


        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setUser(user);
        serviceRequest.setRoom(room);
        serviceRequest.setStay(stay); // Set stay, can be null
        serviceRequest.setType(requestType);
        serviceRequest.setDescription(description);
        serviceRequest.setRequestTime(LocalDateTime.now());
        serviceRequest.setScheduledTime(scheduledTime); // Can be null
        serviceRequest.setStatus(ServiceRequest.RequestStatus.PENDING); // Initial status

        return serviceRequestRepository.save(serviceRequest);
    }

    /**
     * Creates and saves a new Room Service request.
     *
     * @param userId The ID of the user making the request.
     * @param roomNumber The room number for which the service is requested.
     * @param items The specific items requested for room service.
     * @param scheduledTimeStr The scheduled time for the service in HH:MM format.
     * @param stayId Optional ID of the active stay associated with the request (can be null).
     * @return The saved ServiceRequest entity.
     * @throws IllegalArgumentException if user or room is not found, or time format is invalid.
     */
    @Transactional
    public ServiceRequest createRoomServiceRequest(Long userId, String roomNumber, String items, String scheduledTimeStr, Long stayId) {
        LocalDateTime scheduledTime = null;
        if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) {
            try {
                // Try parsing with HH:MM
                scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("HH:mm")));
            } catch (DateTimeParseException e) {
                try {
                    // Try parsing with h:mm a (e.g., 7:30 PM)
                    scheduledTime = LocalDateTime.now().with(java.time.LocalTime.parse(scheduledTimeStr, DateTimeFormatter.ofPattern("h:mm a")));
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid time format. Please use HH:MM (e.g., 19:30) or H:MM AM/PM (e.g., 7:30 PM).", e2);
                }
            }
        }

        String description = "Room Service: " + items;

        return createServiceRequest(userId, roomNumber, ServiceRequest.RequestType.ROOM_SERVICE, description, scheduledTime, stayId);
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
     * @param room The room entity.
     * @return A list of service requests.
     */
    public List<ServiceRequest> getServiceRequestsByRoom(Room room) {
        return serviceRequestRepository.findByRoom(room);
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