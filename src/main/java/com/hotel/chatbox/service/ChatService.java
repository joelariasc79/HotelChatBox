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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ServiceRequestService serviceRequestService;

	@Value("classpath:/prompts/chat-prompt.st")
	private Resource chatPromptTemplate;

	// *****************************************
	// CHECK IN
	// *****************************************

	// Regex Patterns - Defined once for efficiency
	private final Pattern patternCheckIn = Pattern.compile("check[- ]?in.*booking\\s*id\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

	// *****************************************
	// ROOM SERVICE
	// *****************************************

	// Pattern 1: Full room service request - items, room, and optional time/date
	// This pattern now specifically looks for "order," "get," or "send up" (for items)
	// and implicitly excludes "housekeeping."
	private final Pattern patternRoomServiceFull = Pattern.compile(
			"(?i)" +
					"^(?!.*housekeeping)" +                                  // ⛔ Negative lookahead to exclude housekeeping
					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|request)?\\s*(?:a|an|some)?\\s*" +
					"([a-zA-Z\\s]+?)\\s*" +                                   // Group 1: items
					"for room\\s*([Ss]?\\d+)\\s*" +                           // Group 2: room number
					"(?:for\\s*(today|tomorrow)\\s*)?" +                      // Group 3: date context
					"(?:by|at)?\\s*[‘'\"\\u2018\\u2019]?\\s*" +               // optional left quote
					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)\\s*" +              // Group 4: time
					"[’'\"]?"                                                 // optional right quote
			);

	// Pattern 2: Room service with items and time (assuming current user's room, or system will ask)
	// This pattern also focuses on verbs related to getting items and excludes "housekeeping."
	private final Pattern patternRoomServiceItemsTime = Pattern.compile(
			"(?i)" +
					"^(?!.*\\bhousekeeping\\b).*" +  // Exclude if 'housekeeping' is present
					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|send(?: up)?)?\\s*" +
					"([a-zA-Z\\s]+?)\\s*" +                         // Group 1: items
					"(?:delivered to my room)?\\s*(?:by|at)\\s*" +
					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)" // Group 2: time
			);

	// Pattern 3: Room service with items only (will require prompt for room/time)
	// Ensures "room service" is mentioned or the verbs clearly imply ordering.
	private final Pattern patternRoomServiceItemsOnly = Pattern.compile(
			"(?i)" +
					"^(?!.*\\bhousekeeping\\b).*" +  // Still exclude 'housekeeping'
					"(?:I'd like to|Can I)?\\s*(?:order|get)\\s+" +
					"([a-zA-Z\\s]+?)(?:\\s*for room service)?"
			);


	// *****************************************
	// HOUSEKEEPING
	// *****************************************

	// Pattern 1: Full housekeeping request - room and optional time/date
	// This pattern is designed to clearly capture the room number and the full time phrase.
	private final Pattern patternHousekeepingFull = Pattern.compile(
			"(?:(?:I'd like to|Can I|get|Please)\\s*(?:schedule|get)?(?:\\s*a)?\\s*housekeeping(?: service)?)\\s+" + // Extended verbs
					"for room\\s*([Ss]?\\d+)" + // Group 1: Room number
					"(?:\\s+(?:for|by|at)\\s*(tomorrow|today)?(?:\\s*(?:by|at)?)?\\s*[‘'\"\\u2018\\u2019]?(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)[’'\"]?)?", // Groups 2 & 3: date & time with quote tolerance
					Pattern.CASE_INSENSITIVE
			);

	// Pattern 2: Housekeeping with Time, but no explicit room (system would then ask for room)
	private final Pattern patternHousekeepingTimeOnly = Pattern.compile(
			"(?:(?:I'd like to|Can I|Please) (?:schedule)?(?:a)?\\s*housekeeping(?: service)?)\\s+" +
					"(?:for\\s*(tomorrow|today)?\\s*(?:by|at)?)?\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)", // Group 1: tomorrow/today, Group 2: Time
					Pattern.CASE_INSENSITIVE
			);

	// Pattern 3: Simple request for housekeeping (system would then ask for room and time)
	private final Pattern patternHousekeepingSimple = Pattern.compile(
			"(?:(?:I need|I'd like|Can I get|Please send) (?:a)?\\s*housekeeping(?: service)?)",
			Pattern.CASE_INSENSITIVE
			);

	private final Pattern patternEscalateToHuman = Pattern.compile(
			"(?:I want to talk to|connect me with|speak to|I need to speak with|escalate to|transfer me to)\\s*(?:a|the)?\\s*(?:human|agent|representative|manager|someone|person)",
			Pattern.CASE_INSENSITIVE
			);

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
			ApplicationContext applicationContext,
			ServiceRequestService serviceRequestService) {
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
		this.serviceRequestService = serviceRequestService;
	}

	public Mono<String> handleGuestQuery(String guestMessage) {
		System.out.println("guestMessage -1: " + guestMessage);		

		// **************************
		// Human Agent Escalation Start:
		// **************************

		Matcher matcherEscalateToHuman = patternEscalateToHuman.matcher(guestMessage);
		if (matcherEscalateToHuman.find()) {
			System.out.println("DEBUG: Matched Human Agent Escalation request.");
			return Mono.just("Certainly! I'm connecting you with a human agent now. Please wait a moment while I transfer your request.");

			//	    	  System.out.println("DEBUG: Matched Human Agent Escalation request.");
			//	          // START OF NEW CODE TO ADD
			//	          try {
			//	              Long currentUserId = getCurrentUserId();
			//	              User user = userRepository.findById(currentUserId)
			//	                  .orElseThrow(() -> new IllegalArgumentException("User not found for escalation."));
			//
			//	              // Create a feedback entry of type ESCALATION
			//	              Feedback feedback = new Feedback(
			//	                  null, // ID (auto-generated)
			//	                  user,
			//	                  null, // No specific hotel for general escalation
			//	                  null, // No specific booking for general escalation
			//	                  Feedback.FeedbackType.COMPLAINT,
			//	                  "User requested to speak with a human: \"" + guestMessage + "\"",
			//	                  LocalDateTime.now(),
			//	                  Feedback.FeedbackStatus.NEW, // Initial status
			//	                  "Awaiting human agent assignment." // Initial agent notes
			//	              );
			//	              feedbackRepository.save(feedback);
			//	              System.out.println("DEBUG: Escalation request logged for user " + user.getUsername() + ". Feedback ID: " + feedback.getFeedbackId());
			//
			//	              // In a real system, you'd integrate with a live chat, ticketing, or notification system here.
			//	              // Examples (pseudo-code):
			//	              // liveChatService.transferToAgent(user.getUserId(), guestMessage);
			//	              // ticketingService.createTicket(user.getUsername(), "Human Agent Request", guestMessage, feedback.getFeedbackId());
			//	              // notificationService.notifyAgent("User " + user.getUsername() + " needs assistance. Feedback ID: " + feedback.getFeedbackId());
			//
			//	              return Mono.just("Certainly! I've escalated your request to a human agent. They will review your message and get back to you shortly.");
			//
			//	          } catch (IllegalStateException | IllegalArgumentException e) {
			//	              System.err.println("ERROR: Failed to log escalation request due to user context: " + e.getMessage());
			//	              return Mono.just("I'm sorry, I couldn't log your request to a human agent right now. Please try again or reach out directly if the issue is urgent.");
			//	          } catch (Exception e) {
			//	              System.err.println("CRITICAL ERROR: Unexpected error during human escalation: " + e.getMessage());
			//	              e.printStackTrace();
			//	              return Mono.just("I encountered an unexpected issue while trying to connect you to a human agent. Please try again later.");
			//	          }
			//	          // END OF NEW CODE TO ADD
		}

		// **************************
		// Check In:
		// **************************

		Matcher matcherCheckIn = patternCheckIn.matcher(guestMessage);
		if (matcherCheckIn.find()) {
			Long bookingId = Long.parseLong(matcherCheckIn.group(1));
			// Ensure you are creating an instance of the *correct* CheckInRequest
			var checkInFunction = (Function<CheckInRequest, String>) applicationContext.getBean("performCheckIn");
			return Mono.just(checkInFunction.apply(new CheckInRequest(bookingId)));
		}

		// **************************
		// Room Service:
		// **************************

		Matcher matcherRoomServiceFull = patternRoomServiceFull.matcher(guestMessage);
		Matcher matcherRoomServiceItemsTime = patternRoomServiceItemsTime.matcher(guestMessage);
		Matcher matcherRoomServiceItemsOnly = patternRoomServiceItemsOnly.matcher(guestMessage);

		if (matcherRoomServiceFull.find()) {
			String items = matcherRoomServiceFull.group(1).trim();
			String roomNumber = matcherRoomServiceFull.group(2).trim();
			String dateContext = matcherRoomServiceFull.group(3); // Group 3: dateContext
			String timeStr = matcherRoomServiceFull.group(4);     // Group 4: timeStr

			System.out.println("DEBUG RoomService - Full Match:");
			System.out.println("  Items: " + items);
			System.out.println("  Room: " + roomNumber);
			System.out.println("  Date: " + dateContext);
			System.out.println("  Time: " + timeStr);

			return handleRoomServiceRequest(items, dateContext, timeStr, roomNumber, guestMessage);

		} else if (matcherRoomServiceItemsTime.find()) {
			String items = matcherRoomServiceItemsTime.group(1).trim();
			String dateContext = matcherRoomServiceItemsTime.group(2); // Group 2: dateContext
			String timeStr = matcherRoomServiceItemsTime.group(3).trim(); // Group 3: timeStr

			System.out.println("DEBUG RoomService - Matched items+time:");
			System.out.println("  Items: " + items);
			System.out.println("  Date: " + dateContext);
			System.out.println("  Time: " + timeStr);

			String responseTime = (dateContext != null ? dateContext + " " : "") + timeStr;
			return Mono.just("I got your request for " + items + " for " + responseTime + ". What room number should I send it to?");

		} else if (matcherRoomServiceItemsOnly.find()) {
			String items = matcherRoomServiceItemsOnly.group(1).trim();

			System.out.println("DEBUG RoomService - Matched items only:");
			System.out.println("  Items: " + items);

			return Mono.just("You'd like " + items + "? And for what room number and time?");
		}

		// **************************
		// Housekeeping Service:
		// **************************

		Matcher matcherHousekeepingFull = patternHousekeepingFull.matcher(guestMessage);
		Matcher matcherHousekeepingTimeOnly = patternHousekeepingTimeOnly.matcher(guestMessage);
		Matcher matcherHousekeepingSimple = patternHousekeepingSimple.matcher(guestMessage);

		if (matcherHousekeepingFull.find()) {
			String roomNumber = matcherHousekeepingFull.group(1); // Group 1: roomNumber
			String dateContext = matcherHousekeepingFull.group(2); // Group 2: dateContext
			String timeStr = matcherHousekeepingFull.group(3);     // Group 3: timeStr

			System.out.println("DEBUG Housekeeping - Matched Full Pattern:");
			System.out.println("  Room Number: " + roomNumber);
			System.out.println("  Date Context (Group 2): " + dateContext);
			System.out.println("  Time String (Group 3): " + timeStr);

			return handleHousekeepingRequest(dateContext, timeStr, roomNumber, guestMessage);

		} else if (matcherHousekeepingTimeOnly.find()) {
			String dateContext = matcherHousekeepingTimeOnly.group(1); // Group 1: dateContext
			String timeStr = matcherHousekeepingTimeOnly.group(2).trim(); // Group 2: timeStr

			System.out.println("DEBUG Housekeeping - Matched Time Only Pattern:");
			System.out.println("  Date Context (Group 1): " + dateContext);
			System.out.println("  Time String (Group 2): " + timeStr);

			String responseTime = (dateContext != null ? dateContext + " " : "") + timeStr;
			return Mono.just("I can schedule housekeeping for " + responseTime + ". What room number should it be for?");

		} else if (matcherHousekeepingSimple.find()) {
			System.out.println("DEBUG Housekeeping - Matched Simple Pattern.");
			return Mono.just("You'd like housekeeping? And for what room number and time?");
		}

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


	private Mono<String> handleRoomServiceRequest(String items, String dateContext, String timeStr, String roomNumber, String originalMessage) {
		System.out.println("handleRoomServiceRequest");

		LocalDateTime schdDateTime = null;
		if (timeStr != null && !timeStr.isEmpty()) {
			LocalDate requestDate = LocalDate.now(); // Default to today

			// Use the new dateContext parameter
			if (dateContext != null) {
				if (dateContext.equalsIgnoreCase("today")) {
					requestDate = LocalDate.now().plusDays(1);
				} else if (dateContext.equalsIgnoreCase("tomorrow")) {
					requestDate = LocalDate.now().plusDays(1);
				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) {
					requestDate = LocalDate.now().plusDays(2);
				}
				else {
					try {
						// Define a formatter that can parse your date string formats
						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy"); // Example format
						requestDate = LocalDate.parse(dateContext, dateFormatter);
					} catch (DateTimeParseException e) {
						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage());
						// Fallback to today if parsing fails for explicit dates
					}
				}
			}

			try {
				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
				schdDateTime = LocalDateTime.of(requestDate, parsedTime);
			} catch (DateTimeParseException e) {
				try {
					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));
					schdDateTime = LocalDateTime.of(requestDate, parsedTime);
				} catch (DateTimeParseException e2) {
					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage());
					return Mono.just("I couldn't understand the time you provided for room service. Please use a format like '7:00 PM' or '19:00'.");
				}
			}
		}

		LocalDateTime scheduledDateTime = schdDateTime;
		return getRoomServiceRequestContext(roomNumber)
				.flatMap(context -> {
					ServiceRequest roomServiceReq = serviceRequestService.createRoomServiceRequest(
							context.user().getId(), // Use ID from context
							context.room().getRoomId(),   // Use Room ID from context
							items,
							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
							context.stayId()              // Use Stay ID from context (can be null)
							);
					return Mono.just("Your room service for " + items + " to room " + roomNumber +
							(scheduledDateTime != null ? " for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a")) : " now") +
							" has been placed. Request ID: " + roomServiceReq.getRequestId() + ".");
				})
				.onErrorResume(IllegalArgumentException.class, e -> {
					System.err.println("ERROR: Failed to create room service request: " + e.getMessage());
					return Mono.just("I'm sorry, I couldn't complete your room service request: " + e.getMessage());
				})
				.onErrorResume(Exception.class, e -> {
					System.err.println("CRITICAL ERROR: Unexpected error placing room service: " + e.getMessage());
					e.printStackTrace();
					return Mono.just("I encountered an unexpected issue while trying to place your room service request. Please try again later.");
				});
	}

	private Mono<String> handleHousekeepingRequest(String dateContext, String timeStr, String roomNumber, String originalMessage) {
		LocalDateTime schdDateTime = null;
		if (timeStr != null && !timeStr.isEmpty()) {
			LocalDate requestDate = LocalDate.now(); // Default to today

			// Use the new dateContext parameter
			if (dateContext != null) {
				if (dateContext.equalsIgnoreCase("today")) {
					requestDate = LocalDate.now().plusDays(1);
				} else if (dateContext.equalsIgnoreCase("tomorrow")) {
					requestDate = LocalDate.now().plusDays(1);
				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) {
					requestDate = LocalDate.now().plusDays(2);
				}
				else {
					try {
						// Define a formatter that can parse your date string formats
						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy"); // Example format
						requestDate = LocalDate.parse(dateContext, dateFormatter);
					} catch (DateTimeParseException e) {
						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage());
						// Fallback to today if parsing fails for explicit dates
					}
				}
			}

			try {
				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
				schdDateTime = LocalDateTime.of(requestDate, parsedTime);
			} catch (DateTimeParseException e) {
				try {
					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));
					schdDateTime = LocalDateTime.of(requestDate, parsedTime);
				} catch (DateTimeParseException e2) {
					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage());
					return Mono.just("I couldn't understand the time you provided for room service. Please use a format like '7:00 PM' or '19:00'.");
				}
			}
		}

		LocalDateTime scheduledDateTime = schdDateTime;
		// Use the new helper method to get the room and stay context
		return getRoomServiceRequestContext(roomNumber)
				.flatMap(context -> {
					ServiceRequest housekeepingReq = serviceRequestService.scheduleHousekeeping(
							context.user().getId(), // Use ID from context
							context.room().getRoomId(),   // Use Room ID from context
							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
							context.stayId()              // Use Stay ID from context (can be null)
							);
					return Mono.just("Your housekeeping request for room " + roomNumber +
							(scheduledDateTime != null ? " for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a")) : " now") +
							" has been placed. Request ID: " + housekeepingReq.getRequestId() + ".");
				})
				.onErrorResume(IllegalArgumentException.class, e -> {
					System.err.println("ERROR: Failed to create housekeeping request: " + e.getMessage());
					return Mono.just("I'm sorry, I couldn't complete your housekeeping request: " + e.getMessage());
				})
				.onErrorResume(Exception.class, e -> {
					System.err.println("CRITICAL ERROR: Unexpected error placing housekeeping request: " + e.getMessage());
					e.printStackTrace();
					return Mono.just("I encountered an unexpected issue while trying to place your housekeeping request. Please try again later.");
				});
	}

	// New private record to hold the common context for room/stay related requests
	private record RoomRequestContext(User user, Room room, Long stayId) {}


	// New helper method to get the common Room and Stay context
	private Mono<RoomRequestContext> getRoomServiceRequestContext(String roomNumber) {
		Long currentUserId = getCurrentUserId();

		try {
			// 1. Get current user
			User currentUser = userRepository.findById(currentUserId)
					.orElseThrow(() -> new IllegalArgumentException("User with ID " + currentUserId + " not found."));

			// 2. Get list of Stay (IN_PROGRESS) by current user
			List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);

			if (activeStays.isEmpty()) {
				return Mono.error(new IllegalArgumentException("You currently don't have any active stays. I can't place this request without an active reservation."));
			}

			// 3. Extract all Room entities from the Stay List
			List<Room> roomsForActiveStays = activeStays.stream()
					.map(Stay::getRoom)
					.collect(Collectors.toList());

			// 4. Get the Room that match roomNumber in the room_id list
			List<Room> matchingRooms = roomsForActiveStays.stream()
					.filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
					.collect(Collectors.toList());

			if (matchingRooms.isEmpty()) {
				return Mono.error(new IllegalArgumentException("The room number " + roomNumber + " is not associated with any of your current active stays."));
			}

			if (matchingRooms.size() > 1) {
				System.err.println("WARNING: User " + currentUserId + " has multiple active stays with room number " + roomNumber + ". Ambiguity detected.");
				return Mono.error(new IllegalArgumentException("You have multiple active stays in rooms with the number " + roomNumber + ". Could you please specify which hotel this is for?"));
			}

			// If we reach here, we have a unique matching room for the user's active stays
			Room targetRoom = matchingRooms.get(0);

			// Now, find the specific Stay object that corresponds to this targetRoom
			Optional<Stay> foundStay = activeStays.stream()
					.filter(stay -> stay.getRoom().equals(targetRoom))
					.findFirst();

			Long stayId = foundStay.map(Stay::getStayId).orElse(null);
			if (stayId == null) {
				System.err.println("ERROR: Matched room " + roomNumber + " but could not find corresponding active stay. Proceeding without stayId for ServiceRequest.");
			}

			return Mono.just(new RoomRequestContext(currentUser, targetRoom, stayId));

		} catch (IllegalArgumentException e) {
			return Mono.error(e); // Propagate known IllegalArgumentExceptions
		} catch (Exception e) {
			System.err.println("CRITICAL ERROR: Unexpected error in getRoomServiceRequestContext: " + e.getMessage());
			e.printStackTrace();
			return Mono.error(new IllegalStateException("An unexpected error occurred while processing your request context."));
		}
	}



	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("User is not authenticated.");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails) {
			// Assuming your UserDetails implementation (e.g., CustomUserDetails)
			// has a way to get the actual user ID from your database User entity.
			// You will likely need to cast or have a method in your UserDetails
			// to retrieve the specific ID.
			String username = ((UserDetails) principal).getUsername();
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username));
			return user.getId(); // Assuming User entity has getUserId()
		} else if (principal instanceof String) {
			// This might happen if the principal is just the username string
			String username = (String) principal;
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username));
			return user.getId();
		} else {
			throw new IllegalStateException("Unable to retrieve user details from authentication principal.");
		}
	}

}
