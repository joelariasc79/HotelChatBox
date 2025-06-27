package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Amenities;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class ChatServiceFuture {
	@Value("classpath:/prompts/chat-prompt.st")
	private Resource chatPromptTemplate;

//	// *****************************************
//	// NEW BOOKING REQUEST PATTERN
//	// *****************************************
//	private final Pattern patternBookingRequest = Pattern.compile(
//		"(?i)(?:I would like to|Can I|Please)?\\s*(?:book|reserve)\\s*(?:a|an)?\\s*([a-zA-Z\\s]+?)(?:\\s+room)?\\s*(?:in|at)\\s+(.*?)(?:\\s+for\\s+(\\d+)\\s*guests?)?"
//	);
//
//	// *****************************************
//	// CHECK IN
//	// *****************************************
//	private final Pattern patternCheckIn = Pattern.compile("check[- ]?in.*booking\\s*id\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
//
//	// *****************************************
//	// ROOM SERVICE
//	// *****************************************
//	private final Pattern patternRoomServiceFull = Pattern.compile(
//			"(?i)" +
//					"^(?!.*housekeeping)" +
//					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|request)?\\s*(?:a|an|some)?\\s*" +
//					"([a-zA-Z\\s]+?)\\s*" +
//					"for room\\s*([Ss]?\\d+)\\s*" +
//					"(?:for\\s*(today|tomorrow)\\s*)?" +
//					"(?:by|at)?\\s*[‘'\"\\u2018\\u2019]?\\s*" +
//					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)\\s*" +
//					"[’'\"]?"
//			);
//	private final Pattern patternRoomServiceItemsTime = Pattern.compile(
//			"(?i)" +
//					"^(?!.*\\bhousekeeping\\b).*" +
//					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|send(?: up)?)?\\s*" +
//					"([a-zA-Z\\s]+?)\\s*" +
//					"(?:delivered to my room)?\\s*(?:by|at)\\s*" +
//					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)"
//			);
//	private final Pattern patternRoomServiceItemsOnly = Pattern.compile(
//			"(?i)" +
//					"^(?!.*\\bhousekeeping\\b).*" +
//					"(?:I'd like to|Can I)?\\s*(?:order|get)\\s+" +
//					"([a-zA-Z\\s]+?)(?:\\s*for room service)?"
//			);
//
//	// *****************************************
//	// HOUSEKEEPING
//	// *****************************************
//	private final Pattern patternHousekeepingFull = Pattern.compile(
//			"(?:(?:I'd like to|Can I|get|Please)\\s*(?:schedule|get)?(?:\\s*a)?\\s*housekeeping(?: service)?)\\s+" +
//					"for room\\s*([Ss]?\\d+)" +
//					"(?:\\s+(?:for|by|at)\\s*(tomorrow|today)?(?:\\s*(?:by|at)?)?\\s*[‘'\"\\u2018\\u2019]?(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)[’'\"]?)?",
//					Pattern.CASE_INSENSITIVE
//			);
//	private final Pattern patternHousekeepingTimeOnly = Pattern.compile(
//			"(?:(?:I'd like to|Can I|Please) (?:schedule)?(?:a)?\\s*housekeeping(?: service)?)\\s+" +
//					"(?:for\\s*(tomorrow|today)?\\s*(?:by|at)?)?\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)",
//					Pattern.CASE_INSENSITIVE
//			);
//	private final Pattern patternHousekeepingSimple = Pattern.compile(
//			"(?:(?:I need|I'd like|Can I get|Please send) (?:a)?\\s*housekeeping(?: service)?)",
//			Pattern.CASE_INSENSITIVE
//			);
//
//	// *****************************************
//	// FEEDBACK
//	// *****************************************
//	private final Pattern patternFeedbackWithRoom = Pattern.compile(
//		    "(?i)" +
//		    "(?:room\\s*([Ss]?\\d+)\\s*[:,\\-]?\\s*)?(.*?)(?:\\s+room\\s*([Ss]?\\d+)\\b)?",
//		    Pattern.DOTALL
//		);
//	private final Pattern patternIsFeedback = Pattern.compile(
//		    "(?:(?:I'd like to|Can I|get|Please)\\s*(?:schedule|get)?(?:\\s*a)?\\s*feedback(?: service)?|" +
//		    "(?:I need|I'd like|Can I get|Please send)\\s*(?:a)?\\s*feedback(?: service)?)",
//		    Pattern.CASE_INSENSITIVE
//		);
//
//	// *****************************************
//	// MANUAL ESCALATION
//	// *****************************************
//	private final Pattern patternEscalateToHuman = Pattern.compile(
//			"(?:I want to talk to|connect me with|speak to|I need to speak with|escalate to|transfer me to)\\s*(?:a|the)?\\s*(?:human|agent|representative|manager|someone|person)",
//			Pattern.CASE_INSENSITIVE
//			);
//
//	private final ChatModel chatModel;
//	private final VectorStore vectorStore;
//	private final HotelRepository hotelRepository;
//	private final HotelRoomRepository hotelRoomRepository; // Added for booking
//	private final AmenitiesRepository amenitiesRepository;
//	private final FeedbackRepository feedbackRepository;
//	private final UserRepository userRepository;
//	private final FacilityReservationRepository facilityReservationRepository;
//	private final RoomReservationRepository roomReservationRepository;
//	private final BookingRepository bookingRepository;
//	private final RoomRepository roomRepository;
//	private final StayService stayService;
//	private final StayRepository stayRepository;
//	private final ApplicationContext applicationContext;
//	private final ServiceRequestService serviceRequestService;
//	private final FeedbackService feedbackService;
//	private final MessageSource messageSource;
//
//	public ChatService(ChatModel chatModel,
//			VectorStore vectorStore,
//			HotelRepository hotelRepository,
//			HotelRoomRepository hotelRoomRepository, // Added for booking
//			AmenitiesRepository amenitiesRepository,
//			FeedbackRepository feedbackRepository,
//			UserRepository userRepository,
//			FacilityReservationRepository facilityReservationRepository,
//			RoomReservationRepository roomReservationRepository,
//			BookingRepository bookingRepository,
//			RoomRepository roomRepository,
//			StayService stayService,
//			StayRepository stayRepository,
//			ApplicationContext applicationContext,
//			ServiceRequestService serviceRequestService,
//			FeedbackService feedbackService,
//			MessageSource messageSource) {
//		this.chatModel = chatModel;
//		this.vectorStore = vectorStore;
//		this.hotelRepository = hotelRepository;
//		this.hotelRoomRepository = hotelRoomRepository; // Assigned
//		this.amenitiesRepository = amenitiesRepository;
//		this.feedbackRepository = feedbackRepository;
//		this.userRepository = userRepository;
//		this.facilityReservationRepository = facilityReservationRepository;
//		this.roomReservationRepository = roomReservationRepository;
//		this.bookingRepository = bookingRepository;
//		this.roomRepository = roomRepository;
//		this.stayService = stayService;
//		this.stayRepository = stayRepository;
//		this.applicationContext = applicationContext;
//		this.serviceRequestService = serviceRequestService;
//		this.feedbackService = feedbackService;
//		this.messageSource = messageSource;
//	}
//
//	public Mono<String> handleGuestQuery(String guestMessage) {
//		Locale userLocale = LocaleContextHolder.getLocale();
//
//		System.out.println("guestMessage -1: " + guestMessage);
//		System.out.println("User Locale: " + userLocale);
//
//		// **************************
//		// NEW: Booking Request Start
//		// **************************
//		Matcher matcherBooking = patternBookingRequest.matcher(guestMessage);
//		if (matcherBooking.find()) {
//		    String roomType = matcherBooking.group(1).trim();
//		    String hotelName = matcherBooking.group(2).trim();
//		    String guestsStr = matcherBooking.group(3); // Can be null if "for X guests" is omitted
//
//		    int numGuests = 0;
//		    if (guestsStr != null && !guestsStr.isEmpty()) {
//		        try {
//		            numGuests = Integer.parseInt(guestsStr);
//		        } catch (NumberFormatException e) {
//		            System.err.println("ERROR: Could not parse number of guests: " + guestsStr);
//		            return Mono.just(messageSource.getMessage("booking.error.parse_guests", null, userLocale));
//		        }
//		    } else {
//		        // Default to 1 guest if not specified
//		        numGuests = 1;
//		        System.out.println("DEBUG: Number of guests not specified, defaulting to 1.");
//		    }
//		    System.out.println("DEBUG: Matched Booking request:");
//		    System.out.println("  Room Type: " + roomType);
//		    System.out.println("  Hotel Name: " + hotelName);
//		    System.out.println("  Guests: " + numGuests);
//
//		    return handleBookingRequest(roomType, hotelName, numGuests, userLocale);
//		}
//		// **************************
//		// END: Booking Request
//		// **************************
//
//		// **************************
//		// NEW: Skip other handlers if "book" is present (and not caught by specific booking pattern)
//		// **************************
//		boolean skipSpecificHandlersAndGoToAI = false;
//		if (guestMessage.toLowerCase().contains("book")) {
//		    System.out.println("DEBUG: Message contains 'book' keyword but not specific booking pattern. Skipping other handlers, proceeding to AI.");
//		    skipSpecificHandlersAndGoToAI = true;
//		}
//
//		// Only proceed with other specific handlers IF we are NOT skipping to AI
//		if (!skipSpecificHandlersAndGoToAI) {
//		    // **************************
//		    // Human Agent Escalation Start:
//		    // **************************
//		    Matcher matcherEscalateToHuman = patternEscalateToHuman.matcher(guestMessage);
//			if (matcherEscalateToHuman.find()) {
//		        System.out.println("DEBUG: Matched Human Agent Escalation request.");
//		        try {
//		            Long currentUserId = getCurrentUserId();
//		            User user = userRepository.findById(currentUserId)
//		                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_escalation", null, userLocale)));
//
//		            Feedback feedback = new Feedback(
//		                null,
//		                user,
//		                null,
//		                null,
//		                Feedback.FeedbackType.ESCALATION,
//		                "User requested to speak with a human: \"" + guestMessage + "\"",
//		                LocalDateTime.now(),
//		                Feedback.FeedbackStatus.NEW,
//		                "Awaiting human agent assignment."
//		            );
//		            feedbackRepository.save(feedback);
//		            System.out.println("DEBUG: Escalation request logged for user " + user.getUsername() + ". Feedback ID: " + feedback.getFeedbackId());
//
//		            return Mono.just(messageSource.getMessage("human_escalation.success", null, userLocale));
//
//		        } catch (IllegalStateException | IllegalArgumentException e) {
//		            System.err.println("ERROR: Failed to log escalation request due to user context: " + e.getMessage());
//		            return Mono.just(messageSource.getMessage("human_escalation.error", null, userLocale));
//		        } catch (Exception e) {
//		            System.err.println("CRITICAL ERROR: Unexpected error during human escalation: " + e.getMessage());
//		            e.printStackTrace();
//		            return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale));
//		        }
//		    }
//
//		    // **************************
//		    // Check In:
//		    // **************************
//		    Matcher matcherCheckIn = patternCheckIn.matcher(guestMessage);
//			if (matcherCheckIn.find()) {
//		        Long bookingId = Long.parseLong(matcherCheckIn.group(1));
//		        var checkInFunction = (Function<CheckInRequest, String>) applicationContext.getBean("performCheckIn");
//		        String result = checkInFunction.apply(new CheckInRequest(bookingId));
//		        if (result.contains("confirmed")) {
//		            return Mono.just(messageSource.getMessage("checkin.success", new Object[]{bookingId}, userLocale));
//		        } else {
//		            return Mono.just(messageSource.getMessage("checkin.error.not_found", new Object[]{bookingId}, userLocale));
//		        }
//		    }
//
//		    // **************************
//		    // Room Service:
//		    // **************************
//		    Matcher matcherRoomServiceFull = patternRoomServiceFull.matcher(guestMessage);
//		    Matcher matcherRoomServiceItemsTime = patternRoomServiceItemsTime.matcher(guestMessage);
//		    Matcher matcherRoomServiceItemsOnly = patternRoomServiceItemsOnly.matcher(guestMessage);
//
//		    if (matcherRoomServiceFull.find()) {
//		        String items = matcherRoomServiceFull.group(1).trim();
//		        String roomNumber = matcherRoomServiceFull.group(2).trim();
//		        String dateContext = matcherRoomServiceFull.group(3);
//		        String timeStr = matcherRoomServiceFull.group(4);
//
//		        System.out.println("DEBUG RoomService - Full Match:");
//		        System.out.println("  Items: " + items);
//		        System.out.println("  Room: " + roomNumber);
//		        System.out.println("  Date: " + dateContext);
//		        System.out.println("  Time: " + timeStr);
//
//		        return handleRoomServiceRequest(items, dateContext, timeStr, roomNumber, guestMessage, userLocale);
//		    } else if (matcherRoomServiceItemsTime.find()) {
//		        String items = matcherRoomServiceItemsTime.group(1).trim();
//		        String timeStr = matcherRoomServiceItemsTime.group(2).trim();
//	            String dateContext = null;
//
//		        System.out.println("DEBUG RoomService - Matched items+time:");
//		        System.out.println("  Items: " + items);
//		        System.out.println("  Time: " + timeStr);
//		        String responseTime = timeStr;
//		        return Mono.just(messageSource.getMessage("room_service.ask_room", new Object[]{items, responseTime}, userLocale));
//		    } else if (matcherRoomServiceItemsOnly.find()) {
//		        String items = matcherRoomServiceItemsOnly.group(1).trim();
//		        System.out.println("DEBUG RoomService - Matched items only:");
//		        System.out.println("  Items: " + items);
//		        return Mono.just(messageSource.getMessage("room_service.ask_room_time", new Object[]{items}, userLocale));
//		    }
//
//		    // **************************
//		    // Housekeeping Service:
//				// **************************
//		    Matcher matcherHousekeepingFull = patternHousekeepingFull.matcher(guestMessage);
//		    Matcher matcherHousekeepingTimeOnly = patternHousekeepingTimeOnly.matcher(guestMessage);
//		    Matcher matcherHousekeepingSimple = patternHousekeepingSimple.matcher(guestMessage);
//
//		    if (matcherHousekeepingFull.find()) {
//		        String roomNumber = matcherHousekeepingFull.group(1);
//		        String dateContext = matcherHousekeepingFull.group(2);
//		        String timeStr = matcherHousekeepingFull.group(3);
//
//		        System.out.println("DEBUG Housekeeping - Matched Full Pattern:");
//		        System.out.println("  Room Number: " + roomNumber);
//		        System.out.println("  Date Context (Group 2): " + dateContext);
//		        System.out.println("  Time String (Group 3): " + timeStr);
//
//		        return handleHousekeepingRequest(dateContext, timeStr, roomNumber, guestMessage, userLocale);
//		    } else if (matcherHousekeepingTimeOnly.find()) {
//		        String dateContext = matcherHousekeepingTimeOnly.group(1);
//		        String timeStr = matcherHousekeepingTimeOnly.group(2).trim();
//
//		        System.out.println("DEBUG Housekeeping - Matched Time Only Pattern:");
//		        System.out.println("  Date Context (Group 1): " + dateContext);
//		        System.out.println("  Time String (Group 2): " + timeStr);
//		        String responseTime = (dateContext != null ? dateContext + " " : "") + timeStr;
//		        return Mono.just(messageSource.getMessage("housekeeping.ask_room", new Object[]{responseTime}, userLocale));
//		    } else if (matcherHousekeepingSimple.find()) {
//		        System.out.println("DEBUG Housekeeping - Matched Simple Pattern.");
//		        return Mono.just(messageSource.getMessage("housekeeping.ask_room_time", null, userLocale));
//		    }
//
//		    // **************************
//		    // Feedback:
//		    // **************************
//		    Matcher matcher = patternFeedbackWithRoom.matcher(guestMessage);
//			if (matcher.matches()) {
//		        String roomStart = matcher.group(1);
//		        String message = matcher.group(2).trim();
//		        String roomEnd = matcher.group(3);
//
//		        String roomNumber = roomStart != null ? roomStart : roomEnd;
//		        System.out.println("Room: " + (roomNumber != null ? roomNumber : "not provided"));
//		        System.out.println("Feedback: " + message);
//
//		        return handleFeedback(roomNumber, guestMessage, userLocale);
//		    }
//		} // End of if (!skipSpecificHandlersAndGoToAI)
//
//	    // **************************
//	    // Prompt :
//	    // **************************
//	    List<Document> relevantDocuments = vectorStore.similaritySearch(
//	            SearchRequest.builder().query(guestMessage).topK(3).build());
//	    String context = relevantDocuments.stream()
//	            .map(doc -> {
//	                String currentContent = doc.getFormattedContent();
//	                String type = (String) doc.getMetadata().get("type");
//	                Integer sourceId = (Integer) doc.getMetadata().get("source_id");
//
//	                if (sourceId != null) {
//	                    if ("hotel_description".equals(type)) {
//	                        Optional<Hotel> hotel = hotelRepository.findById(sourceId);
//	                        return hotel.map(h -> "Hotel Information: " + h.getHotelName() + " - " + h.getDescription() + " Amenities: " + h.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | ")
//	                                .orElse(currentContent);
//	                    } else if ("room_description".equals(type)) {
//	                        Optional<HotelRoom> room = hotelRoomRepository.findById(sourceId);
//	                        return room.map(r -> "Room Information: " + r.getDescription() + " Price: $" + r.getPrice() + " Amenities: " + r.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | \n")
//	                                .orElse(currentContent);
//	                    } else if ("amenity_info".equals(type)) {
//	                        Optional<Amenities> amenity = amenitiesRepository.findById(sourceId);
//	                        return amenity.map(a -> "Amenity: " + a.getName() + " | ")
//	                                .orElse(currentContent);
//	                    }
//	                }
//	                return currentContent;
//	            })
//	            .collect(Collectors.joining("\n---\n"));
//
//	    PromptTemplate promptTemplate = new PromptTemplate(chatPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
//	    Prompt prompt = promptTemplate.create(
//	            Map.of("context", context, "question", guestMessage, "locale", userLocale.toLanguageTag())
//	            );
//
//	    System.out.println("DEBUG: Sending prompt to AI ChatModel: " + prompt.getContents());
//
//	    try {
//	        String aiResponseContent = chatModel.call(prompt).getResult().getOutput().getText();
//	        System.out.println("DEBUG: aiResponseContent: " + aiResponseContent);
//	        return Mono.just(aiResponseContent);
//	    } catch (Exception e) {
//	        System.err.println("Error processing AI response: " + e.getMessage());
//	        return Mono.just(messageSource.getMessage("error.unexpected_ai_response", null, userLocale));
//	    }
//	}
//
//	// *****************************************
//	// handleBookingRequest Method
//	// *****************************************
//	private Mono<String> handleBookingRequest(String roomType, String hotelName, int numGuests, Locale userLocale) {
//	    // 1. Basic validation/lookup for Hotel and Room Type
//	    Optional<Hotel> hotelOptional = hotelRepository.findByHotelNameContainingIgnoreCase(hotelName);
//	    if (hotelOptional.isEmpty()) {
//	        return Mono.just(messageSource.getMessage("booking.error.no_hotel", new Object[]{hotelName}, userLocale));
//	    }
//	    Hotel hotel = hotelOptional.get();
//
//	    // Check if the requested room type exists for this hotel (case-insensitive)
//	    // You might want a more sophisticated check here (e.g., matching capacity, specific amenities)
//	    Optional<HotelRoom> hotelRoomOptional = hotelRoomRepository.findByHotelAndRoomTypeContainingIgnoreCase(hotel, roomType);
//	    if (hotelRoomOptional.isEmpty()) {
//	        return Mono.just(messageSource.getMessage("booking.error.no_hotel_room_type", new Object[]{roomType, hotelName}, userLocale));
//	    }
//	    // HotelRoom hotelRoom = hotelRoomOptional.get(); // You could use this if you needed room details
//
//	    // 2. Determine proposed dates (placeholder logic for now as per example's answer format)
//	    // Example: Check-in 3 days from now, stay for 3 nights
//	    LocalDate checkinDate = LocalDate.now().plusDays(3);
//	    LocalDate checkoutDate = checkinDate.plusDays(3);
//
//	    // Format dates for the response
//	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", userLocale);
//	    String formattedCheckin = checkinDate.format(dateFormatter);
//	    String formattedCheckout = checkoutDate.format(dateFormatter);
//
//	    // 3. Construct the response using MessageSource
//	    return Mono.just(messageSource.getMessage("booking.confirmation",
//	            new Object[]{roomType, hotelName, numGuests, formattedCheckin, formattedCheckout},
//	            userLocale));
//	}
//
//
//	private Mono<String> handleRoomServiceRequest(String items, String dateContext, String timeStr, String roomNumber, String originalMessage, Locale userLocale) {
//		System.out.println("handleRoomServiceRequest");
//
//		LocalDateTime schdDateTime = null;
//		if (timeStr != null && !timeStr.isEmpty()) {
//			LocalDate requestDate = LocalDate.now();
//
//			if (dateContext != null) {
//				if (dateContext.equalsIgnoreCase("today")) {
//					requestDate = LocalDate.now();
//				} else if (dateContext.equalsIgnoreCase("tomorrow")) {
//					requestDate = LocalDate.now().plusDays(1);
//				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) {
//					requestDate = LocalDate.now().plusDays(2);
//				}
//				else {
//					try {
//						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd,yyyy", userLocale);
//						requestDate = LocalDate.parse(dateContext, dateFormatter);
//					} catch (DateTimeParseException e) {
//						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage());
//					}
//				}
//			}
//
//			try {
//				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
//				schdDateTime = LocalDateTime.of(requestDate, parsedTime);
//			} catch (DateTimeParseException e) {
//				try {
//					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a", userLocale));
//					schdDateTime = LocalDateTime.of(requestDate, parsedTime);
//				} catch (DateTimeParseException e2) {
//					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage());
//					return Mono.just(messageSource.getMessage("error.parse_time", null, userLocale));
//				}
//			}
//		}
//
//		LocalDateTime scheduledDateTime = schdDateTime;
//		return getRoomNumberContext(roomNumber, userLocale)
//				.flatMap(context -> {
//					ServiceRequest roomServiceReq = serviceRequestService.createRoomServiceRequest(
//							context.user().getId(),
//							context.room().getRoomId(),
//							items,
//							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
//							context.stayId()
//							);
//					String formattedScheduledTime = (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a", userLocale)) : messageSource.getMessage("now", null, userLocale));
//					return Mono.just(messageSource.getMessage("room_service.confirm", new Object[]{items, roomNumber, formattedScheduledTime, roomServiceReq.getRequestId()}, userLocale));
//				})
//				.onErrorResume(IllegalArgumentException.class, e -> {
//					System.err.println("ERROR: Failed to create room service request: " + e.getMessage());
//					if (e.getMessage().contains("active stays")) {
//                         return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//                    } else if (e.getMessage().contains("room number")) {
//                         return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//                    } else if (e.getMessage().contains("multiple active stays")) {
//                         return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//                    } else if (e.getMessage().contains("User with ID")) {
//                    	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//                    }
//					return Mono.just(messageSource.getMessage("error.unexpected_room_service", new Object[]{e.getMessage()}, userLocale));
//				})
//				.onErrorResume(Exception.class, e -> {
//					System.err.println("CRITICAL ERROR: Unexpected error placing room service: " + e.getMessage());
//					e.printStackTrace();
//					return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale));
//				});
//	}
//
//	private Mono<String> handleHousekeepingRequest(String dateContext, String timeStr, String roomNumber, String originalMessage, Locale userLocale) {
//		LocalDateTime schdDateTime = null;
//		if (timeStr != null && !timeStr.isEmpty()) {
//			LocalDate requestDate = LocalDate.now();
//
//			if (dateContext != null) {
//				if (dateContext.equalsIgnoreCase("today")) {
//					requestDate = LocalDate.now();
//				} else if (dateContext.equalsIgnoreCase("tomorrow")) {
//					requestDate = LocalDate.now().plusDays(1);
//				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) {
//					requestDate = LocalDate.now().plusDays(2);
//				}
//				else {
//					try {
//						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd,yyyy", userLocale);
//						requestDate = LocalDate.parse(dateContext, dateFormatter);
//					} catch (DateTimeParseException e) {
//						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage());
//					}
//				}
//			}
//
//			try {
//				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
//				schdDateTime = LocalDateTime.of(requestDate, parsedTime);
//			} catch (DateTimeParseException e) {
//				try {
//					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a", userLocale));
//					schdDateTime = LocalDateTime.of(requestDate, parsedTime);
//				} catch (DateTimeParseException e2) {
//					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage());
//					return Mono.just(messageSource.getMessage("error.parse_time", null, userLocale));
//				}
//			}
//		}
//
//		LocalDateTime scheduledDateTime = schdDateTime;
//		return getRoomNumberContext(roomNumber, userLocale)
//				.flatMap(context -> {
//					ServiceRequest housekeepingReq = serviceRequestService.scheduleHousekeeping(
//							context.user().getId(),
//							context.room().getRoomId(),
//							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
//							context.stayId()
//							);
//					String formattedScheduledTime = (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a", userLocale)) : messageSource.getMessage("now", null, userLocale));
//					return Mono.just(messageSource.getMessage("housekeeping.confirm", new Object[]{roomNumber, formattedScheduledTime, housekeepingReq.getRequestId()}, userLocale));
//				})
//				.onErrorResume(IllegalArgumentException.class, e -> {
//					System.err.println("ERROR: Failed to create housekeeping request: " + e.getMessage());
//					if (e.getMessage().contains("active stays")) {
//                         return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//                    } else if (e.getMessage().contains("room number")) {
//                         return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//                    } else if (e.getMessage().contains("multiple active stays")) {
//                         return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//                    } else if (e.getMessage().contains("User with ID")) {
//                    	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//                    }
//					return Mono.just(messageSource.getMessage("error.unexpected_housekeeping", new Object[]{e.getMessage()}, userLocale));
//				})
//				.onErrorResume(Exception.class, e -> {
//					System.err.println("CRITICAL ERROR: Unexpected error placing housekeeping request: " + e.getMessage());
//					e.printStackTrace();
//					return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale));
//				});
//	}
//
//	private Mono<String> handleFeedback(String roomNumber, String originalMessage, Locale userLocale) {
//	    Long currentUserId = getCurrentUserId();
//
//	    if (roomNumber != null && !roomNumber.trim().isEmpty()) {
//	        return getRoomNumberContext(roomNumber, userLocale)
//	                .flatMap(context -> {
//	                    Feedback savedFeedback = feedbackService.saveFeedback(
//	                            context.user().getId(),
//	                            context.room().getRoomId(),
//	                            Feedback.FeedbackType.GENERAL,
//	                            originalMessage,
//	                            context.stayId()
//	                    );
//	                    return Mono.just(messageSource.getMessage("feedback.room_submitted", new Object[]{roomNumber, savedFeedback.getFeedbackId()}, userLocale));
//	                })
//	                .onErrorResume(IllegalArgumentException.class, e -> {
//	                    System.err.println("ERROR: Failed to submit feedback for a specific room: " + e.getMessage());
//	                    if (e.getMessage().contains("active stays")) {
//	                         return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//	                    } else if (e.getMessage().contains("room number")) {
//	                         return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//	                    } else if (e.getMessage().contains("multiple active stays")) {
//	                         return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//	                    } else if (e.getMessage().contains("User with ID")) {
//	                    	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//	                    }
//	                    return Mono.just(messageSource.getMessage("error.unexpected_feedback_room", new Object[]{e.getMessage()}, userLocale));
//	                })
//	                .onErrorResume(Exception.class, e -> {
//	                    System.err.println("CRITICAL ERROR: Unexpected error submitting feedback for specific room: " + e.getMessage());
//	                    e.printStackTrace();
//	                    return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale));
//	                });
//	    } else {
//	        return Mono.fromCallable(() -> {
//	            User currentUser = userRepository.findById(currentUserId)
//	                    .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_feedback", null, userLocale)));
//
//	            Long inferredRoomId = null;
//	            Long inferredStayId = null;
//	            String inferredRoomNumber = null;
//
//	            List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);
//
//	            if (!activeStays.isEmpty()) {
//	                Stay activeStay = activeStays.get(0);
//	                if (activeStay.getRoom() != null) {
//	                    inferredRoomId = activeStay.getRoom().getRoomId();
//	                    inferredRoomNumber = activeStay.getRoom().getRoomNumber();
//	                }
//	                inferredStayId = activeStay.getStayId();
//	                if (activeStays.size() > 1) {
//	                    System.out.println("WARNING: User " + currentUserId + " has multiple active stays. Using the first found stay (ID: " + inferredStayId + ", Room: " + inferredRoomNumber + ") for feedback context.");
//	                } else {
//	                    System.out.println("DEBUG: User " + currentUserId + " has no active stays. Submitting general feedback without room/stay context.");
//	                }
//	            } else {
//	                System.out.println("DEBUG: User " + currentUserId + " has no active stays. Submitting general feedback without room/stay context.");
//	            }
//
//	            Feedback savedFeedback = feedbackService.saveFeedback(
//	                    currentUser.getId(),
//	                    inferredRoomId,
//	                    Feedback.FeedbackType.GENERAL,
//	                    originalMessage,
//	                    inferredStayId
//	            );
//	            if (inferredRoomNumber != null) {
//	                return messageSource.getMessage("feedback.room_inferred_submitted", new Object[]{inferredRoomNumber, savedFeedback.getFeedbackId()}, userLocale);
//	            } else {
//	                return messageSource.getMessage("feedback.general_submitted", new Object[]{savedFeedback.getFeedbackId()}, userLocale);
//	            }
//	        })
//	        .onErrorResume(IllegalArgumentException.class, e -> {
//	            System.err.println("ERROR: Failed to submit general feedback: " + e.getMessage());
//	            return Mono.just(messageSource.getMessage("error.unexpected_feedback_general", new Object[]{e.getMessage()}, userLocale));
//	        })
//	        .onErrorResume(Exception.class, e -> {
//	            System.err.println("CRITICAL ERROR: Unexpected error submitting general feedback: " + e.getMessage());
//	            e.printStackTrace();
//	            return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale));
//	        });
//	    }
//	}
//
//	private record RoomNumbertContext(User user, Room room, Long stayId) {}
//
//	private Mono<RoomNumbertContext> getRoomNumberContext(String roomNumber, Locale userLocale) {
//		Long currentUserId = getCurrentUserId();
//		try {
//			User currentUser = userRepository.findById(currentUserId)
//					.orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_context", new Object[]{currentUserId}, userLocale)));
//			List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);
//			if (activeStays.isEmpty()) {
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.no_active_stays", null, userLocale)));
//			}
//
//			List<Room> roomsForActiveStays = activeStays.stream()
//					.map(Stay::getRoom)
//					.collect(Collectors.toList());
//			List<Room> matchingRooms = roomsForActiveStalls.stream()
//					.filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
//					.collect(Collectors.toList());
//			if (matchingRooms.isEmpty()) {
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale)));
//			}
//
//			if (matchingRooms.size() > 1) {
//				System.err.println("WARNING: User " + currentUserId + " has multiple active stays with room number " + roomNumber + ". Ambiguity detected.");
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale)));
//			}
//
//			Room targetRoom = matchingRooms.get(0);
//			Optional<Stay> foundStay = activeStays.stream()
//					.filter(stay -> stay.getRoom().equals(targetRoom))
//					.findFirst();
//			Long stayId = foundStay.map(Stay::getStayId).orElse(null);
//			if (stayId == null) {
//				System.err.println("ERROR: Matched room " + roomNumber + " but could not find corresponding active stay. Proceeding without stayId for ServiceRequest.");
//			}
//
//			return Mono.just(new RoomNumbertContext(currentUser, targetRoom, stayId));
//
//		} catch (IllegalArgumentException e) {
//			return Mono.error(e);
//		} catch (Exception e) {
//			System.err.println("CRITICAL ERROR: Unexpected error in getRoomServiceRequestContext: " + e.getMessage());
//			e.printStackTrace();
//			return Mono.error(new IllegalStateException(messageSource.getMessage("error.unexpected_context", null, userLocale)));
//		}
//	}
//
//	private Long getCurrentUserId() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (authentication == null || !authentication.isAuthenticated()) {
//			throw new IllegalStateException("User is not authenticated.");
//		}
//
//		Object principal = authentication.getPrincipal();
//		if (principal instanceof UserDetails) {
//			String username = ((UserDetails) principal).getUsername();
//			User user = userRepository.findByUsername(username)
//					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username));
//			return user.getId();
//		} else if (principal instanceof String) {
//			String username = (String) principal;
//			User user = userRepository.findByUsername(username)
//					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username));
//			return user.getId();
//		} else {
//			throw new IllegalStateException("Unable to retrieve user details from authentication principal.");
//		}
//	}
}