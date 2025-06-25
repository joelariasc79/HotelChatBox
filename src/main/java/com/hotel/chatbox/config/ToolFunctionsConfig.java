// src/main/java/com/hotel/chatbox/config/ToolFunctionsConfig.java
package com.hotel.chatbox.config;

import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.ServiceRequest;

import com.hotel.chatbox.repository.BookingRepository;
import com.hotel.chatbox.repository.RoomRepository;
import com.hotel.chatbox.repository.RoomReservationRepository;
import com.hotel.chatbox.repository.StayRepository;
import com.hotel.chatbox.repository.UserRepository;

import com.hotel.chatbox.service.StayService;
import com.hotel.chatbox.service.ServiceRequestService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// Import all your request records from the new common location
//import com.hotel.chatbox.model.requests.ToolRequests.RoomBookingRequest;
//import com.hotel.chatbox.model.requests.ToolRequests.RoomServiceRequest;
import com.hotel.chatbox.model.requests.ToolRequests.HousekeepingRequest;
import com.hotel.chatbox.model.requests.ToolRequests.SpaReservationRequest;
import com.hotel.chatbox.model.requests.ToolRequests.GymReservationRequest;
import com.hotel.chatbox.model.requests.ToolRequests.CheckInRequest;


@Configuration
public class ToolFunctionsConfig {

    private final UserRepository userRepository;
    private final RoomReservationRepository roomReservationRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final StayService stayService;
    private final StayRepository stayRepository;
    private final ServiceRequestService serviceRequestService;

    public ToolFunctionsConfig(UserRepository userRepository,
                               RoomReservationRepository roomReservationRepository,
                               BookingRepository bookingRepository,
                               RoomRepository roomRepository,
                               StayService stayService,
                               StayRepository stayRepository,
                               ServiceRequestService serviceRequestService) {
        this.userRepository = userRepository;
        this.roomReservationRepository = roomReservationRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.stayService = stayService;
        this.stayRepository = stayRepository;
        this.serviceRequestService = serviceRequestService;
    }
    
//    @Bean(name = "scheduleHousekeeping")
//    @Transactional // Keep transactional here as the service method is also transactional
//    public Function<HousekeepingRequest, String> scheduleHousekeeping() {
//        return request -> {
//            try {
//                System.out.println("DEBUG: scheduleHousekeeping called with time=" + request.time() + ", roomNumber=" + request.room_id() + "request_id=" + request.stay_id());
//
//                // For a real application, userId should come from the authenticated user context.
//                // For this example, we'll assume a default user or fetch it if needed.
//                User currentUser = userRepository.findByUsername("user").orElse(null);
//                if (currentUser == null) {
//                    System.err.println("ERROR: User 'user' not found for housekeeping service. Ensure a user with username 'user' exists in the database.");
//                    return "I cannot place a housekeeping request without identifying you. Please ensure you are logged in.";
//                }
//
//                // Call the new service method
//                ServiceRequest newServiceRequest = serviceRequestService.scheduleHousekeeping(
//                    currentUser.getId(),
//                    request.room_id(),
//                    request.time(), 
//                    request.stay_id()
//                );
//
//                return "Your room housekeeping request at " + request.time() +
//                       " for room " + request.room_id() + " has been placed. Request ID: " + newServiceRequest.getRequestId() + ".";
//
//            } catch (IllegalArgumentException e) {
//                System.err.println("ERROR: Error creating housekeeping request: " + e.getMessage());
//                return "I apologize, but I couldn't process your housekeeping request: " + e.getMessage();
//            } catch (Exception e) {
//                System.err.println("CRITICAL ERROR: Exception caught during scheduleHousekeeping. Full stack trace below:");
//                e.printStackTrace();
//                return "I apologize, but I encountered an unexpected issue while trying to place your housekeeping request. Please try again later.";
//            }
//        };
//    }

//    @Bean(name = "scheduleHousekeeping")
//    @Transactional
//    public Function<HousekeepingRequest, String> scheduleHousekeeping() {
//        return request -> {
//            try {
//                User currentUser = userRepository.findByUsername("user").orElse(null);
//                if (currentUser == null) {
//                    return "I cannot schedule housekeeping without identifying you. Please ensure you are logged in.";
//                }
//
//                LocalDate parsedDate = LocalDate.parse(request.date());
//                LocalTime parsedTime = LocalTime.parse(request.time());
//
//                FacilityReservation reservation = new FacilityReservation(
//                    currentUser,
//                    FacilityReservation.FacilityType.HOUSEKEEPING,
//                    parsedDate,
//                    parsedTime,
//                    "Room: " + request.roomNumber()
//                );
//                facilityReservationRepository.save(reservation);
//                return "Housekeeping for room " + request.roomNumber() + " has been scheduled for " + request.date() + " at " + request.time() + ". Confirmation ID: " + reservation.getReservationId() + ".";
//            } catch (DateTimeParseException e) {
//                return "I couldn't understand the date or time format for housekeeping. Please provide date in YYYY-MM-DD and time in HH:MM.";
//            } catch (Exception e) {
//                System.err.println("Error scheduling housekeeping: " + e.getMessage());
//                return "I apologize, but I encountered an issue while trying to schedule housekeeping. Please try again later.";
//            }
//        };
//    }
//
//    @Bean(name = "createSpaReservation")
//    @Transactional
//    public Function<SpaReservationRequest, String> createSpaReservation() {
//        return request -> {
//            try {
//                User currentUser = userRepository.findByUsername("user").orElse(null);
//                if (currentUser == null) {
//                    return "I cannot make a spa reservation without identifying you. Please ensure you are logged in.";
//                }
//
//                LocalDate parsedDate = LocalDate.parse(request.date());
//                LocalTime parsedTime = LocalTime.parse(request.time());
//
//                FacilityReservation reservation = new FacilityReservation(
//                    currentUser,
//                    FacilityReservation.FacilityType.SPA,
//                    parsedDate,
//                    parsedTime,
//                    "Service: " + request.service()
//                );
//                facilityReservationRepository.save(reservation);
//                return "Your spa reservation for a " + request.service() + " on " + request.date() + " at " + request.time() + " has been confirmed. Confirmation ID: " + reservation.getReservationId() + ".";
//            } catch (DateTimeParseException e) {
//                return "I couldn't understand the date or time format for the spa reservation. Please provide date in YYYY-MM-DD and time in HH:MM.";
//            } catch (Exception e) {
//                System.err.println("Error creating spa reservation: " + e.getMessage());
//                return "I apologize, but I encountered an issue while trying to make your spa reservation. Please try again later.";
//            }
//        };
//    }
//
//    @Bean(name = "createGymReservation")
//    @Transactional
//    public Function<GymReservationRequest, String> createGymReservation() {
//        return request -> {
//            try {
//                User currentUser = userRepository.findByUsername("user").orElse(null);
//                if (currentUser == null) {
//                    return "I cannot make a gym reservation without identifying you. Please ensure you are logged in.";
//                }
//
//                LocalDate parsedDate = LocalDate.parse(request.date());
//                LocalTime parsedTime = LocalTime.parse(request.time());
//
//                FacilityReservation reservation = new FacilityReservation(
//                    currentUser,
//                    FacilityReservation.FacilityType.GYM,
//                    parsedDate,
//                    parsedTime,
//                    "Gym access"
//                );
//                facilityReservationRepository.save(reservation);
//                return "Your gym access for " + request.date() + " at " + request.time() + " has been reserved. Confirmation ID: " + reservation.getReservationId() + ".";
//            } catch (DateTimeParseException e) {
//                return "I couldn't understand the date or time format for the gym reservation. Please provide date in YYYY-MM-DD and time in HH:MM.";
//            } catch (Exception e) {
//                System.err.println("Error creating gym reservation: " + e.getMessage());
//                return "I apologize, but I encountered an issue while trying to make your gym reservation. Please try again later.";
//            }
//        };
//    }

    @Bean(name = "performCheckIn")
    @Transactional
    public Function<CheckInRequest, String> performCheckIn() {
        return request -> {
            System.out.println("DEBUG: performCheckIn called with bookingId=" + request.bookingId());

            User currentUser = userRepository.findByUsername("user").orElse(null);
            if (currentUser == null) {
                System.err.println("ERROR: User 'user' not found for check-in. Ensure a user with username 'user' exists in the database.");
                return "I cannot perform check-in without identifying you. Please ensure you are logged in.";
            }

            Optional<Booking> bookingOpt = bookingRepository.findById(request.bookingId());
            if (bookingOpt.isEmpty()) {
                System.err.println("ERROR: Booking not found with ID: " + request.bookingId());
                return "I could not find a booking with the provided booking ID: " + request.bookingId() + ". Please confirm the ID.";
            }
            Booking booking = bookingOpt.get();

            // Validate that the booking belongs to the current user
            if (!booking.getUser().getId().equals(currentUser.getId())) {
                System.err.println("ERROR: Booking ID " + request.bookingId() + " does not belong to user " + currentUser.getUsername());
                return "The booking ID " + request.bookingId() + " does not belong to your account.";
            }

            // --- NEW LOGIC: Find an available room based on booking's room type ---
            HotelRoom bookedHotelRoomType = booking.getHotelRoom();
            if (bookedHotelRoomType == null) {
                System.err.println("ERROR: Booking " + request.bookingId() + " does not have an associated hotel room type.");
                return "The booking details are incomplete. No room type found for your booking.";
            }

            // Find an available physical room that matches the booked room type
            List<Room> availableRooms = roomRepository.findByHotelRoomTypeAndAvailabilityStatus(
                        bookedHotelRoomType, Room.RoomAvailabilityStatus.AVAILABLE);

            if (availableRooms.isEmpty()) {
                System.err.println("ERROR: No available rooms of type " + bookedHotelRoomType.getType().getName() + " for booking " + request.bookingId());
                return "Unfortunately, there are no available " + bookedHotelRoomType.getType().getName() + " rooms for check-in at this moment. Please contact the front desk.";
            }

            Room physicalRoom = availableRooms.get(0); // Select the first available room

            // --- Existing checks, now applied to the found room ---
            // Check for overlapping stays for this physical room (more robust availability)
            List<Stay> overlappingStays = stayRepository.findByRoomAndStayStatus(physicalRoom, Stay.StayStatus.IN_PROGRESS);
            if (!overlappingStays.isEmpty()) {
                System.err.println("CRITICAL: Selected physical room " + physicalRoom.getRoomNumber() + " has existing ACTIVE stays. Manual intervention required.");
                return "There seems to be an issue with room " + physicalRoom.getRoomNumber() + "'s availability. It might be occupied. Please contact the front desk.";
            }

            try {
                // Call the StayService to create the Stay (check-in)
                Stay newStay = stayService.createStay(booking.getBookingId(), currentUser.getId(), physicalRoom.getRoomId());

                // Update physical room status.
                physicalRoom.setAvailabilityStatus(Room.RoomAvailabilityStatus.OCCUPIED);
                roomRepository.save(physicalRoom);

                // Optionally, update booking status to reflect check-in if not already handled
                if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                    booking.setStatus(Booking.BookingStatus.COMPLETED); // Or a specific CHECKED_IN status if you define one
                    bookingRepository.save(booking);
                }

                return "Check-in successful! You are now checked into room " + physicalRoom.getRoomNumber() + " for Booking ID " + newStay.getBooking().getBookingId() + ". Your stay ID is: " + newStay.getStayId() + ".";
            } catch (Exception e) {
                System.err.println("Error saving stay (check-in): " + e.getMessage());
                e.printStackTrace();
                return "I apologize, but I encountered an error while trying to complete your check-in. Please try again later.";
            }
        };
    }
}