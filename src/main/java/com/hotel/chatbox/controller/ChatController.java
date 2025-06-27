package com.hotel.chatbox.controller;

import com.hotel.chatbox.service.BookingService;
import com.hotel.chatbox.service.ChatService; // Import the new ChatService 
import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.dto.ChatResponse;
import com.hotel.chatbox.parser.BookingResponseParser;
import com.hotel.chatbox.parser.HotelParser;
import com.hotel.chatbox.parser.BookingResponseParser.BookingDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import reactor.core.publisher.Mono;


/**
 * REST Controller for handling chat requests from the frontend.
 * Delegates the actual chat logic to the ChatService.
 */
@RestController
public class ChatController {

    private final ChatService chatService;
    private final HotelParser hotelParser;
    private final BookingService bookingService; // Inject your new BookingService

    // Constructor injection for dependencies
    public ChatController(ChatService chatService, HotelParser hotelParser, BookingService bookingService) {
        this.chatService = chatService;
        this.hotelParser = hotelParser;
        this.bookingService = bookingService; // Initialize BookingService
    }
    
    @GetMapping("/chat")
    public Mono<ChatResponse> chat(@RequestParam String message, Principal principal) {
        System.out.println("Received chat message: " + message);
        
        String username = null;
        if (principal != null) {
            username = principal.getName(); // Get the username of the logged-in user
            System.out.println("Received chat message from user: " + username + " - Message: " + message);
        } else {
            System.out.println("Received chat message from unauthenticated user: " + message);
        }
        

        Mono<String> rawResponseMono = chatService.handleGuestQuery(message);
        String rawAiResponse = rawResponseMono.block(); // Blocking for simplicity in example

        List<HotelParser.HotelInfo> parsedHotels = null;

        if (hotelParser.canBeParsed(rawAiResponse)) {
            parsedHotels = hotelParser.parse(rawAiResponse);
        }        

//        // --- Logic to trigger booking ---
//        BookingResponseParser parser = new BookingResponseParser();
//        
//        if (parser.canBeParsed(rawAiResponse)) {
//        	
//            BookingDetails details = parser.parseBookingConfirmation(rawAiResponse);
//            
//            
//            if (details != null) {
////                System.out.println("RoomType: " + details.roomType());
////                System.out.println("Hotel Name: " + details.hotelName());
////                System.out.println("Guests Number: " + details.numberOfGuests());
////                System.out.println("Check-in Date: " + details.checkInDate());
////                System.out.println("Check-out Date: " + details.checkOutDate());
//                try {
//                    int numberOfRooms = 1; 
//
//                    Booking booked = bookingService.createBooking(
//                            username,
//                            details.hotelName(),
//                            details.roomType(),
//                            details.numberOfGuests(),
//                            details.checkInDate(),
//                            details.checkOutDate(),
//                            numberOfRooms
//                    );
//                    System.out.println("Booking successfully created with ID: " + booked.getBookingId());
//
//                    return Mono.just(new ChatResponse("Your reservation has been successfully made! Booking ID: " + booked.getBookingId(), null));
//
//                } catch (Exception e) {
//                    System.err.println("Error creating booking: " + e.getMessage());
//                    return Mono.just(new ChatResponse("I apologize, but I encountered an error trying to complete your reservation: " + e.getMessage(), parsedHotels));
//                }
//                
//            }
//	        } else {
//	            System.out.println("Cannot be parsed as a booking confirmation.");
//	        }
//        
//        
//
//        // --- End of booking logic ---

        // --- Return the combined response ---
        if (parsedHotels != null && !parsedHotels.isEmpty()) {
            System.out.println("Hotel List: ");
            parsedHotels.forEach(s -> System.out.println(s));
            return Mono.just(new ChatResponse(rawAiResponse, parsedHotels));
        } else {
            return Mono.just(new ChatResponse(rawAiResponse));
        }
    }
    
}
