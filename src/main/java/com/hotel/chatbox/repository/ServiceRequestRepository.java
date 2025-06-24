package com.hotel.chatbox.repository;

import com.hotel.chatbox.domain.Room; // Import Room entity, not HotelRoom, for this specific query
import com.hotel.chatbox.domain.ServiceRequest;
import com.hotel.chatbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    // Find all service requests by a specific user
    List<ServiceRequest> findByUser(User user);

    // Corrected method: Find all service requests for a specific physical room
    List<ServiceRequest> findByRoom(Room room); // Changed from findByHotelRoom to findByRoom and parameter type to Room

    // Find service requests by type (e.g., ROOM_SERVICE, HOUSEKEEPING)
    List<ServiceRequest> findByType(ServiceRequest.RequestType type);

    // Find service requests by status (e.g., PENDING, COMPLETED)
    List<ServiceRequest> findByStatus(ServiceRequest.RequestStatus status);

    // Find service requests by user and status
    List<ServiceRequest> findByUserAndStatus(User user, ServiceRequest.RequestStatus status);

    // Find pending/in-progress requests scheduled for a specific time or later
    List<ServiceRequest> findByScheduledTimeAfterAndStatusNot(LocalDateTime time, ServiceRequest.RequestStatus status);
}