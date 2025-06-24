// src/main/java/com/hotel/chatbox/service/ChatService.java
package com.hotel.chatbox.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hotel.chatbox.domain.Amenities;
//import com.hotel.chatbox.domain.Booking;
import com.hotel.chatbox.domain.Feedback;
import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.ServiceRequest;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;

import com.hotel.chatbox.repository.AmenitiesRepository;
import com.hotel.chatbox.repository.BookingRepository;
import com.hotel.chatbox.repository.FeedbackRepository;
import com.hotel.chatbox.repository.HotelRepository;
import com.hotel.chatbox.repository.HotelRoomRepository;
import com.hotel.chatbox.repository.RoomRepository;
import com.hotel.chatbox.repository.RoomReservationRepository;
import com.hotel.chatbox.repository.StayRepository;
import com.hotel.chatbox.repository.UserRepository;
import com.hotel.chatbox.repository.FacilityReservationRepository;

import com.hotel.chatbox.model.requests.ToolRequests.CheckInRequest;
//import com.hotel.chatbox.model.requests.ToolRequests.RoomServiceRequest;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.context.ApplicationContext;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Function;


@Service
public class ChatService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final HotelRepository hotelRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final AmenitiesRepository amenitiesRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FacilityReservationRepository facilityReservationRepository;
    private final RoomReservationRepository roomReservationRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final StayService stayService;
    private final StayRepository stayRepository;
    private final ApplicationContext applicationContext;

    @Value("classpath:/prompts/chat-prompt.st")
    private Resource chatPromptTemplate;

    public ChatService(ChatModel chatModel,
                       VectorStore vectorStore,
                       HotelRepository hotelRepository,
                       HotelRoomRepository hotelRoomRepository,
                       AmenitiesRepository amenitiesRepository,
                       FeedbackRepository feedbackRepository,
                       UserRepository userRepository,
                       FacilityReservationRepository facilityReservationRepository,
                       RoomReservationRepository roomReservationRepository,
                       BookingRepository bookingRepository,
                       RoomRepository roomRepository,
                       StayService stayService,
                       StayRepository stayRepository,
                       ApplicationContext applicationContext) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.hotelRepository = hotelRepository;
        this.hotelRoomRepository = hotelRoomRepository;
        this.amenitiesRepository = amenitiesRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.facilityReservationRepository = facilityReservationRepository;
        this.roomReservationRepository = roomReservationRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.stayService = stayService;
        this.stayRepository = stayRepository;
        this.applicationContext = applicationContext;
    }
    

    public Mono<String> handleGuestQuery(String guestMessage) {
        System.out.println("guestMessage -1: " + guestMessage);

        // **************************
        // Check In Start:
        // **************************
        Pattern patternCheckIn = Pattern.compile("check[- ]?in.*booking\\s*id\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcherCheckIn = patternCheckIn.matcher(guestMessage);
        if (matcherCheckIn.find()) {
            Long bookingId = Long.parseLong(matcherCheckIn.group(1));
            // Ensure you are creating an instance of the *correct* CheckInRequest
            var checkInFunction = (Function<CheckInRequest, String>) applicationContext.getBean("performCheckIn");
            return Mono.just(checkInFunction.apply(new CheckInRequest(bookingId)));
        }
        // **************************
        // Check In End:
        // **************************
        
        
        // **************************
        // Room Service Start:
        // **************************
        
//        // --- Room Service Patterns ---
//        // Pattern 1: Highly specific - items, room, time (with optional date for "tomorrow")
//        // Example: I'd like to order a burger for room S100 for tomorrow by 7:00 pm
//        // Example: Please send up a bottle of red wine to room S100 at 21:00
//        
//        Pattern patternRoomServiceFull = Pattern.compile(
//            "(?:(?:I'd like to|Can I|Please) (?:order|get|send(?: up)?))\\s+" + // Start phrase
//            "(.*?)" + // Group 1: Items (non-greedy)
//            "\\s+(?:for|to) room\\s*([Ss]?\\d+)" + // Group 2: Room number (S<digits> or <digits>)
//            "(?:\\s+(?:for|by|at)\\s+(?:tomorrow|today)?\\s*(?:by|at)?\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?))?", // Group 3: Optional Time (and optional "tomorrow"/"today")
//            Pattern.CASE_INSENSITIVE
//        );
//        Matcher matcherRoomServiceFull = patternRoomServiceFull.matcher(guestMessage);
//
//        // Pattern 2: Items and Time (assuming current user's room, or system will ask)
//        // Example: Can I get breakfast delivered to my room by 7 AM?
//        // Example: Order pizza for me at 20:30
//        Pattern patternRoomServiceItemsTime = Pattern.compile(
//            "(?:(?:I'd like to|Can I|Please) (?:order|get|send(?: up)?))?\\s*" + // Optional start phrase
//            "(.*?)" + // Group 1: Items
//            "\\s+(?:delivered to my room)?\\s*(?:by|at)\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)", // Group 2: Time
//            Pattern.CASE_INSENSITIVE
//        );
//        Matcher matcherRoomServiceItemsTime = patternRoomServiceItemsTime.matcher(guestMessage);
//
//        // Pattern 3: Items only (will require prompt for room/time)
//        // Example: I'd like to order a club sandwich and a soda for room service
//        System.out.println("here: 2");
//        Pattern patternRoomServiceItemsOnly = Pattern.compile(
//            "(?:(?:I'd like to|Can I) (?:order|get))\\s+(.*?)(?: for room service)?",
//            Pattern.CASE_INSENSITIVE
//        );
//        Matcher matcherRoomServiceItemsOnly = patternRoomServiceItemsOnly.matcher(guestMessage);
//        
////        System.out.println("patternRoomServiceFull: " + matcherRoomServiceFull.find());
////        System.out.println("matcherRoomServiceItemsTime: " + matcherRoomServiceItemsTime.find());
////        System.out.println("matcherRoomServiceItemsOnly: " + matcherRoomServiceItemsOnly.find());
//
//
//        // --- Processing Room Service Intents ---
//        if (matcherRoomServiceFull.find()) {
//        	System.out.println("here 4 ");
//            String items = matcherRoomServiceFull.group(1).trim();
//            String roomNumber = matcherRoomServiceFull.group(2).trim();
//            String timeStr = matcherRoomServiceFull.group(3); 
//            
//            System.out.println("here 5 ");
//            System.out.println("items: " + items);
//            System.out.println("roomNumber: " + roomNumber);
//            System.out.println("timeStr: " + timeStr);
//
//            return handleRoomServiceRequest(items, timeStr, roomNumber, guestMessage);
//
//        } else if (matcherRoomServiceItemsTime.find()) {
//            String items = matcherRoomServiceItemsTime.group(1).trim();
//            String timeStr = matcherRoomServiceItemsTime.group(2).trim();
//            // Assuming default room for the user if not specified, or prompt for it
//            // For now, let's use a placeholder or assume the system has context.
//            // For this example, we'll make it a requirement for the full request.
//            System.out.println("DEBUG: Matched items+time. Need to handle default room or prompt.");
//            return Mono.just("I got your request for " + items + " at " + timeStr + ". What room number should I send it to?");
//
//        } else if (matcherRoomServiceItemsOnly.find()) {
//            String items = matcherRoomServiceItemsOnly.group(1).trim();
//            System.out.println("DEBUG: Matched items only. Need to handle prompt for time/room.");
//            return Mono.just("You'd like " + items + "? And for what room number and time?");
//
//        }
        
        // **************************
        // Room Service End:
        // **************************
       

        List<Document> relevantDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(guestMessage).topK(3).build());
        String context = relevantDocuments.stream()
                .map(doc -> {
                    String currentContent = doc.getFormattedContent();
                    String type = (String) doc.getMetadata().get("type");
                    Integer sourceId = (Integer) doc.getMetadata().get("source_id");

                    if (sourceId != null) {
                        if ("hotel_description".equals(type)) {
                            Optional<Hotel> hotel = hotelRepository.findById(sourceId);
                            return hotel.map(h -> "Hotel Information: " + h.getHotelName() + " - " + h.getDescription() + " Amenities: " + h.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | ")
                                         .orElse(currentContent);
                        } else if ("room_description".equals(type)) {
                            Optional<HotelRoom> room = hotelRoomRepository.findById(sourceId);
                            return room.map(r -> "Room Information: " + r.getDescription() + " Price: $" + r.getPrice() + " Amenities: " + r.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | ")
                                         .orElse(currentContent);
                        } else if ("amenity_info".equals(type)) {
                            Optional<Amenities> amenity = amenitiesRepository.findById(sourceId);
                            return amenity.map(a -> "Amenity: " + a.getName() + " | ")
                                         .orElse(currentContent);
                        }
                    }
                    return currentContent;
                })
                .collect(Collectors.joining("\n---\n"));

        PromptTemplate promptTemplate = new PromptTemplate(chatPromptTemplate);
        Prompt prompt = promptTemplate.create(
                Map.of("context", context, "question", guestMessage)
        );

        System.out.println("DEBUG: Sending prompt to AI ChatModel: " + prompt.getContents());

        try {
            String aiResponseContent = chatModel.call(prompt).getResult().getOutput().getText();

            System.out.println("DEBUG: aiResponseContent: " + aiResponseContent);

            String finalChatResponse = aiResponseContent;
            String feedbackConfirmation = "";
            Pattern feedbackPattern = Pattern.compile("\\{\\s*\"feedback\":\\s*\\{.*?\\}\\s*\\}", Pattern.DOTALL);
            Matcher matcher = feedbackPattern.matcher(aiResponseContent);

            if (matcher.find()) {
                String feedbackJsonString = matcher.group();
                System.out.println("DEBUG: Found feedback JSON: " + feedbackJsonString);

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Map<String, String>> parsedFeedback = objectMapper.readValue(feedbackJsonString,
                            new TypeReference<Map<String, Map<String, String>>>() {});

                    Map<String, String> feedbackData = parsedFeedback.get("feedback");
                    if (feedbackData != null && feedbackData.containsKey("type") && feedbackData.containsKey("message")) {
                        String typeStr = feedbackData.get("type");
                        String messageStr = feedbackData.get("message");

                        Feedback.FeedbackType feedbackType = null;
                        try {
                            feedbackType = Feedback.FeedbackType.valueOf(typeStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.err.println("Unknown feedback type from AI: " + typeStr + ". Defaulting to GENERAL.");
                            feedbackType = Feedback.FeedbackType.GENERAL;
                        }

                        User currentUser = userRepository.findByUsername("user").orElse(null);
                        Hotel feedbackHotel = null;
                        Optional<Document> hotelDoc = relevantDocuments.stream()
                                .filter(doc -> "hotel_description".equals(doc.getMetadata().get("type")) && doc.getMetadata().containsKey("source_id"))
                                .findFirst();

                        if (hotelDoc.isPresent()) {
                            Integer hotelId = (Integer) hotelDoc.get().getMetadata().get("source_id");
                            if (hotelId != null) {
                                feedbackHotel = hotelRepository.findById(hotelId).orElse(null);
                                if (feedbackHotel != null) {
                                    System.out.println("DEBUG: Associated feedback with Hotel: " + feedbackHotel.getHotelName() + " (ID: " + feedbackHotel.getHotelId() + ")");
                                } else {
                                    System.out.println("DEBUG: Could not find Hotel with ID from document: " + hotelId);
                                }
                            }
                        }

                        if (currentUser != null) {
                            Feedback feedback = new Feedback(
                                null,
                                currentUser,
                                feedbackHotel,
                                null,
                                feedbackType,
                                messageStr,
                                LocalDateTime.now(),
                                Feedback.FeedbackStatus.NEW,
                                null
                            );
                            if (feedbackType == Feedback.FeedbackType.COMPLAINT && (messageStr.toLowerCase().contains("urgent") || messageStr.toLowerCase().contains("escalate") || messageStr.toLowerCase().contains("manager"))) {
                                feedback.setStatus(Feedback.FeedbackStatus.ESCALATED);
                                feedback.setAgentNotes("AI detected urgent complaint, escalated automatically.");
                                feedbackConfirmation = "\nYour urgent complaint has been logged and escalated to a human agent. We will get back to you shortly.";
                            } else {
                                feedback.setStatus(Feedback.FeedbackStatus.NEW);
                                feedbackConfirmation = "\nThank you for your feedback! It has been logged.";
                            }
                            feedbackRepository.save(feedback);
                            System.out.println("DEBUG: Feedback saved to DB: " + feedback.getMessage() + " (Type: " + feedback.getType() + ", Status: " + feedback.getStatus() + ")");
                        } else {
                            System.err.println("DEBUG: User 'user' not found, skipping feedback save.");
                            feedbackConfirmation = "\nI would like to log your feedback, but I couldn't identify you. Please ensure you are logged in.";
                        }
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Error parsing feedback JSON from AI response: " + e.getMessage());
                }

                finalChatResponse = matcher.replaceFirst("").trim();
            }

            return Mono.just(finalChatResponse + feedbackConfirmation);
        } catch (Exception e) {
            System.err.println("Error processing AI response or function call: " + e.getMessage());
            return Mono.just("I'm sorry, I encountered an error while processing your request. Please try again later.");
        }
    }
    
    
    private Mono<String> handleRoomServiceRequest(String items, String timeStr, String roomNumber, String originalMessage) {
        // Authenticated user ID (replace 1L with actual user ID from security context)
        Long currentUserId = 1L;

        System.out.println("Aqui 0");

        // Determine the scheduled time
        LocalDateTime scheduledDateTime = null;
        if (timeStr != null && !timeStr.isEmpty()) {
            System.out.println("Aqui 1");
            LocalDate requestDate = LocalDate.now(); // Default to today
            if (originalMessage.toLowerCase().contains("tomorrow")) {
                requestDate = LocalDate.now().plusDays(1);
            }
            // Parse time, combining with the determined date
            try {
                LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                scheduledDateTime = LocalDateTime.of(requestDate, parsedTime);
            } catch (DateTimeParseException e) {
                try {
                    LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));
                    scheduledDateTime = LocalDateTime.of(requestDate, parsedTime);
                } catch (DateTimeParseException e2) {
                    System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage());
                    return Mono.just("I couldn't understand the time you provided for room service. Please use a format like '7:00 PM' or '19:00'.");
                }
            }
        }

        System.out.println("Aqui 2");

        Long stayId = null; // Will store the stayId if a unique match is found
//        Room targetRoom = null; // Will store the matched Room entity

        try {
            // 1. Get current user
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + currentUserId + " not found."));

            System.out.println("Aqui 2.1 - User found: " + currentUser.getUsername());

            // 2. Get list of Stay (IN_PROGRESS) by current user
            // Assuming StayService has a method like findStaysByUserAndStayStatus
            List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);

            if (activeStays.isEmpty()) {
                return Mono.just("You currently don't have any active stays. I can't place a room service request without an active reservation.");
            }

            System.out.println("Aqui 2.2 - Active stays found: " + activeStays.size());

            // 3. Extract all Room entities from the Stay List
            List<Room> roomsForActiveStays = activeStays.stream()
                                                     .map(Stay::getRoom)
                                                     .collect(Collectors.toList());

            System.out.println("Aqui 2.3 - Rooms associated with active stays: " + roomsForActiveStays.size());

            // 4. Get the Room that match roomNumber in the room_id list
            // Filter roomsForActiveStays to find the one matching the requested roomNumber
            List<Room> matchingRooms = roomsForActiveStays.stream()
                                                        .filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
                                                        .collect(Collectors.toList());

            if (matchingRooms.isEmpty()) {
                return Mono.just("The room number " + roomNumber + " is not associated with any of your current active stays.");
            }

            if (matchingRooms.size() > 1) {
                // This scenario means the user has multiple active stays in different hotels
                // where rooms have the *same room number*. This is highly unlikely
                // for a single user, but if your data model allows it,
                // you might need to prompt the user for more info (e.g., hotel name).
                System.err.println("WARNING: User " + currentUserId + " has multiple active stays with room number " + roomNumber + ". Ambiguity detected.");
                return Mono.just("You have multiple active stays in different rooms named " + roomNumber + ". Could you please specify which hotel this is for?");
            }

            // If we reach here, we have a unique matching room for the user's active stays
            Room targetRoom = matchingRooms.get(0);
            System.out.println("Aqui 2.4 - Matched target room: " + targetRoom.getRoomNumber() + " (ID: " + targetRoom.getRoomId() + ")");

            // Now, find the specific Stay object that corresponds to this targetRoom
            Optional<Stay> foundStay = activeStays.stream()
                                                  .filter(stay -> stay.getRoom().equals(targetRoom))
                                                  .findFirst();

            if (foundStay.isPresent()) {
                stayId = foundStay.get().getStayId();
                System.out.println("Aqui 2.5 - Found active stay ID: " + stayId + " for matched room.");
            } else {
                // This case should theoretically not be reached if targetRoom came from activeStays
                System.err.println("ERROR: Matched room " + roomNumber + " but could not find corresponding active stay. Proceeding without stayId.");
            }

            System.out.println("Aqui 3");
            ServiceRequestService service = (ServiceRequestService) applicationContext.getBean("serviceRequestService");
            System.out.println("Aqui 4");
            System.out.println("currentUserId: " + currentUserId);
            System.out.println("roomNumber: " + roomNumber);
            System.out.println("items: " + items);
            System.out.println("scheduledDateTime: " + scheduledDateTime);
            System.out.println("scheduledDateTime (formatted for service): " + (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null));
            System.out.println("stayId (passed to service): " + stayId);

            ServiceRequest roomServiceReq = service.createRoomServiceRequest(
                currentUserId,
                roomNumber, // Pass the original room number
                items,
                (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null),
                stayId // Pass the found stayId (can be null if not found or ambiguity)
            );
            System.out.println("Aqui 5");
            return Mono.just("Your room service for " + items + " to room " + roomNumber +
                             (scheduledDateTime != null ? " for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a")) : " now") +
                             " has been placed. Request ID: " + roomServiceReq.getRequestId() + ".");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Failed to create room service request: " + e.getMessage());
            return Mono.just("I'm sorry, I couldn't complete your room service request: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Unexpected error placing room service: " + e.getMessage());
            e.printStackTrace();
            return Mono.just("I encountered an unexpected issue while trying to place your room service request. Please try again later.");
        }
    }
    
}
