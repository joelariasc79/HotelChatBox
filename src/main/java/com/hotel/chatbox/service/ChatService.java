package com.hotel.chatbox.service;

import com.hotel.chatbox.domain.Amenities;
import com.hotel.chatbox.domain.Feedback;
import com.hotel.chatbox.domain.Hotel;
import com.hotel.chatbox.domain.HotelRoom;
import com.hotel.chatbox.domain.Room;
import com.hotel.chatbox.domain.ServiceRequest;
import com.hotel.chatbox.domain.Stay;
import com.hotel.chatbox.domain.User;
import com.hotel.chatbox.domain.Booking;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Locale;


@Service
public class ChatService {
	@Value("classpath:/prompts/chat-prompt.st")
	private Resource chatPromptTemplate;

	private final Map<Long, BookingRequest> ongoingBookingRequests = new ConcurrentHashMap<>();

	// Booking Request Class
	public record BookingRequest(
			String hotelName,
			String roomType,
			Integer numberOfGuests,
			LocalDate checkInDate,
			LocalDate checkOutDate
			) {}

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



	// *****************************************
	// FEEDBACK
	// *****************************************
	
	private final Pattern patternIsFeedback = Pattern.compile(
		    "(?i)(?:\\b(feedback|complaint|suggestion|issue|problem|opinion|I think|I feel|I'm not happy|I'm upset|is bad|is great|too slow|too dirty|unacceptable|love this|hate this))\\b"
		);
	
	private final Pattern patternFeedbackWithRoom = Pattern.compile(
		    "(?i)(?:room\\s*([Ss]?\\d+)\\s*[:,\\-]?\\s*)?(.*?)(?:\\s+room\\s*([Ss]?\\d+)\\b)?",
		    Pattern.DOTALL
		);
	

	// *****************************************
	// MANUAL ESCALATION
	// *****************************************

	private final Pattern patternEscalateToHuman = Pattern.compile(
			"(?:I want to talk to|connect me with|speak to|I need to speak with|escalate to|transfer me to)\\s*(?:a|the)?\\s*(?:human|agent|representative|manager|someone|person)",
			Pattern.CASE_INSENSITIVE
			);

	// *****************************************
	// BOOKING
	// *****************************************
	// Pattern for Sequence 1: Full booking details in one go
	private final Pattern patternBookFull = Pattern.compile(
			"(?i).*book(?: a)?\\s*(standard|deluxe|suite)?\\s*room(?: in)?\\s*(.+?)\\s*(?: for\\s*(\\d+)\\s*guests?)?\\s*,?\\s*the check-in date is\\s*(\\d{2}/\\d{2}/\\d{4})\\s*and the check out date is\\s*(\\d{2}/\\d{2}/\\d{4}).*"
			);


	// Pattern for booking a room in a specific hotel (after hotel search)
	private final Pattern patternBookSpecificHotel = Pattern.compile(
			"(?i).*book(?: a)?(?: room)?(?: in(?: the)?)?\\s+(.+?)(?:\\s*hotel)?[\\s\\.]*$"
			);


	// Pattern for capturing remaining booking details (dates, guests, room type)
	private final Pattern patternBookingDetails = Pattern.compile(
			"(?i)(?:(?:the )?check-?in date is\\s*(\\d{2}/\\d{2}/\\d{4})\\s*(?:,|and)?\\s*check-?out date is\\s*(\\d{2}/\\d{2}/\\d{4}))?" +
					"(?:.*?\\bI would like a\\s*(standard|deluxe|suite))?" +
					"(?:.*?we are\\s*(\\d+)\\s*guests?)?"
			);

	// Pattern patternBookingFinalStep	
	private final Pattern patternBookingFinalStep = Pattern.compile(
			"(?i).*check[-\\s]?in date is\\s*(\\d{2}/\\d{2}/\\d{4})\\s*(?:,|and)?\\s*the?\\s*check[-\\s]?out date is\\s*(\\d{2}/\\d{2}/\\d{4}).*?" +
					"\\b(?:i would like (?:a|to request a))\\s*(standard|deluxe|suite)\\s*room?\\s*(?:for|with)?\\s*(\\d+)\\s*guests?.*"
			);


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
	private final FeedbackService feedbackService;
	private final BookingService bookingService;
	//	private final MessageSource messageSource;

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
			ServiceRequestService serviceRequestService,
			FeedbackService feedbackService,
			BookingService bookingService
			//			,MessageSource messageSource
			) {
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
		this.feedbackService = feedbackService;
		this.bookingService = bookingService;
		//		this.messageSource = messageSource;
	}

	public Mono<String> handleGuestQuery(String guestMessage /* , Locale userLocale */) {
		//		Locale userLocale = LocaleContextHolder.getLocale(); 

		System.out.println("guestMessage -1: " + guestMessage);
		//	    System.out.println("User Locale: " + userLocale);

		// **************************
		// Booking:
		// **************************

		long currentUserId = getCurrentUserId();

		// Check for Sequence 1: Full booking details in one go
		Matcher matcherBookFull = patternBookFull.matcher(guestMessage);
		if (matcherBookFull.matches()) {
			System.out.println("DEBUG: Matched 'book full' request.");
			String roomType = matcherBookFull.group(1);
			String hotelName = matcherBookFull.group(2);
			Integer numberOfGuests = matcherBookFull.group(3) != null ? Integer.parseInt(matcherBookFull.group(3)) : null;
			LocalDate checkInDate = parseDate(matcherBookFull.group(4));
			LocalDate checkOutDate = parseDate(matcherBookFull.group(5));

			ongoingBookingRequests.remove(currentUserId);
			return processBooking(currentUserId, hotelName, roomType, numberOfGuests, checkInDate, checkOutDate, 1);
		}

		// Sequence 2 - Step 1: User selects a hotel to book after results shown
		Matcher matcherBookSpecificHotel = patternBookSpecificHotel.matcher(guestMessage);
		if (matcherBookSpecificHotel.matches()) {
			System.out.println("DEBUG: Matched 'book specific hotel' request.");
			String hotelName = matcherBookSpecificHotel.group(1).trim();
			ongoingBookingRequests.put(currentUserId, new BookingRequest(hotelName, null, null, null, null));
			//		    System.out.println("DEBUG: Matched 'book specific hotel' request.: " + ongoingBookingRequests.containsKey(currentUserId));
			//		    System.out.println("DEBUG: Matched 'book specific hotel' request.: " + ongoingBookingRequests.get(currentUserId));
			return Mono.just("Please provide me with the dates you would like to stay, the number of guests, and the type of room you prefer (Standard, Deluxe, or Suite).");
		}

		// Sequence 2 - Step 2: User provides dates, room type, guests
		if (ongoingBookingRequests.containsKey(currentUserId)) {
			System.out.println("DEBUG: Here 0 User provides dates, room type, guests: ");
			BookingRequest partialBooking = ongoingBookingRequests.get(currentUserId);
			System.out.println("DEBUG: Here 1 User provides dates, room type, guests: " + partialBooking.hotelName());

			// Final booking message with all details (from follow-up)
			Matcher matcherBookingFinalStep = patternBookingFinalStep.matcher(guestMessage);
			System.out.println("DEBUG: Here 2 User provides dates, room type, guests: ");
			if (matcherBookingFinalStep.matches()) {
				System.out.println("DEBUG: Here 3 User provides dates, room type, guests: ");
				System.out.println("DEBUG: Matched 'booking final step' with prior hotel context.");

				String roomType = matcherBookingFinalStep.group(3);
				Integer numberOfGuests = Integer.parseInt(matcherBookingFinalStep.group(4));
				LocalDate checkInDate = parseDate(matcherBookingFinalStep.group(1));
				LocalDate checkOutDate = parseDate(matcherBookingFinalStep.group(2));

				System.out.println("DEBUG: Here 4 User provides dates, room type, guests: ");

				ongoingBookingRequests.remove(currentUserId);
				System.out.println("DEBUG: Here 5 User provides dates, room type, guests: ");
				return processBooking(currentUserId, partialBooking.hotelName(), roomType, numberOfGuests, checkInDate, checkOutDate, 1);
			}

			// Otherwise, fallback to partial matcher
			Matcher matcherBookingDetails = patternBookingDetails.matcher(guestMessage);
			if (matcherBookingDetails.matches()) {
				System.out.println("DEBUG: Matched 'booking details' for ongoing request.");

				LocalDate checkInDate = partialBooking.checkInDate();
				LocalDate checkOutDate = partialBooking.checkOutDate();
				String roomType = partialBooking.roomType();
				Integer numberOfGuests = partialBooking.numberOfGuests();

				if (matcherBookingDetails.group(1) != null && matcherBookingDetails.group(2) != null) {
					checkInDate = parseDate(matcherBookingDetails.group(1));
					checkOutDate = parseDate(matcherBookingDetails.group(2));
				}
				if (matcherBookingDetails.group(3) != null) {
					roomType = matcherBookingDetails.group(3);
				}
				if (matcherBookingDetails.group(4) != null) {
					numberOfGuests = Integer.parseInt(matcherBookingDetails.group(4));
				}

				if (checkInDate != null && checkOutDate != null && roomType != null && numberOfGuests != null) {
					ongoingBookingRequests.remove(currentUserId);
					return processBooking(currentUserId, partialBooking.hotelName(), roomType, numberOfGuests, checkInDate, checkOutDate, 1);
				} else {
					ongoingBookingRequests.put(currentUserId, new BookingRequest(
							partialBooking.hotelName(), roomType, numberOfGuests, checkInDate, checkOutDate
							));
					return Mono.just("I still need the following information: " +
							(checkInDate == null || checkOutDate == null ? "check-in and check-out dates, " : "") +
							(numberOfGuests == null ? "number of guests, " : "") +
							(roomType == null ? "room type (Standard, Deluxe, or Suite), " : "") +
							"to complete your booking.");
				}
			}

			System.out.println("DEBUG: Guest message did not match any expected booking patterns.");
			return getAiResponse(guestMessage);
		}

		//		// **************************
		//		// Human Agent Escalation Start:
		//		// **************************
		//
		//		Matcher matcherEscalateToHuman = patternEscalateToHuman.matcher(guestMessage);
		//		if (matcherEscalateToHuman.find()) {
		//			System.out.println("DEBUG: Matched Human Agent Escalation request.");
		//			return Mono.just("Certainly! I'm connecting you with a human agent now. Please wait a moment while I transfer your request.");
		//			System.out.println("DEBUG: Matched Human Agent Escalation request.");
		//			// START OF NEW CODE TO ADD
		//			try {
		//				Long currentUserId = getCurrentUserId();
		//				User user = userRepository.findById(currentUserId)
		//						.orElseThrow(() -> new IllegalArgumentException("User not found for escalation."));
		//
		//				// Create a feedback entry of type ESCALATION
		//				Feedback feedback = new Feedback(
		//						null, // ID (auto-generated)
		//
		//						user,
		//						null, // No specific hotel for general escalation
		//						null, // No specific booking for general escalation
		//
		//						Feedback.FeedbackType.COMPLAINT,
		//						"User requested to speak with a human: \"" + guestMessage + "\"",
		//						LocalDateTime.now(),
		//						Feedback.FeedbackStatus.NEW, // Initial status
		//
		//						//	                  "Awaiting human agent assignment." // Initial agent notes
		//						);
		//				feedbackRepository.save(feedback);
		//				System.out.println("DEBUG: Escalation request logged for user " + user.getUsername() + ". Feedback ID: " + feedback.getFeedbackId());
		//
		//				// In a real system, you'd integrate with a live chat, ticketing, or notification system here.
		//				// Examples (pseudo-code):
		//				// liveChatService.transferToAgent(user.getUserId(), guestMessage);
		//				// ticketingService.createTicket(user.getUsername(), "Human Agent Request", guestMessage, feedback.getFeedbackId());
		//				// notificationService.notifyAgent("User " + user.getUsername() + " needs assistance. Feedback ID: " + feedback.getFeedbackId());
		//
		//				return Mono.just("Certainly! I've escalated your request to a human agent. They will review your message and get back to you shortly.");
		//
		//			} catch (IllegalStateException | IllegalArgumentException e) {
		//				System.err.println("ERROR: Failed to log escalation request due to user context: " + e.getMessage());
		//				return Mono.just("I'm sorry, I couldn't log your request to a human agent right now. Please try again or reach out directly if the issue is urgent.");
		//			} catch (Exception e) {
		//				System.err.println("CRITICAL ERROR: Unexpected error during human escalation: " + e.getMessage());
		//				e.printStackTrace();
		//				return Mono.just("I encountered an unexpected issue while trying to connect you to a human agent. Please try again later.");
		//			}
		//			// END OF NEW CODE TO ADD
		//		}

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
			String timeStr = matcherRoomServiceFull.group(4);
			// Group 4: timeStr

			System.out.println("DEBUG RoomService - Full Match:");
			System.out.println("  Items: " + items);
			System.out.println("  Room: " + roomNumber);
			System.out.println("  Date: " + dateContext);
			System.out.println("  Time: " + timeStr);

			return handleRoomServiceRequest(items, dateContext, timeStr, roomNumber, guestMessage);
		} else if (matcherRoomServiceItemsTime.find()) {
			String items = matcherRoomServiceItemsTime.group(1).trim();
			String dateContext = matcherRoomServiceItemsTime.group(2);
			// Group 2: dateContext
			String timeStr = matcherRoomServiceItemsTime.group(3).trim();
			// Group 3: timeStr

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
			String roomNumber = matcherHousekeepingFull.group(1);
			// Group 1: roomNumber
			String dateContext = matcherHousekeepingFull.group(2);
			// Group 2: dateContext
			String timeStr = matcherHousekeepingFull.group(3);
			// Group 3: timeStr

			System.out.println("DEBUG Housekeeping - Matched Full Pattern:");
			System.out.println("  Room Number: " + roomNumber);
			System.out.println("  Date Context (Group 2): " + dateContext);
			System.out.println("  Time String (Group 3): " + timeStr);

			return handleHousekeepingRequest(dateContext, timeStr, roomNumber, guestMessage);
		} else if (matcherHousekeepingTimeOnly.find()) {
			String dateContext = matcherHousekeepingTimeOnly.group(1);
			// Group 1: dateContext
			String timeStr = matcherHousekeepingTimeOnly.group(2).trim();
			// Group 2: timeStr

			System.out.println("DEBUG Housekeeping - Matched Time Only Pattern:");
			System.out.println("  Date Context (Group 1): " + dateContext);
			System.out.println("  Time String (Group 2): " + timeStr);
			String responseTime = (dateContext != null ? dateContext + " " : "") + timeStr;
			return Mono.just("I can schedule housekeeping for " + responseTime + ". What room number should it be for?");
		} else if (matcherHousekeepingSimple.find()) {
			System.out.println("DEBUG Housekeeping - Matched Simple Pattern.");
			return Mono.just("You'd like housekeeping? And for what room number and time?");
		}


		// **************************
		// Feedback:
		// **************************

//		Matcher matcher = patternFeedbackWithRoom.matcher(guestMessage);
//		if (matcher.matches()) {
//			String roomStart = matcher.group(1);
//			String message = matcher.group(2).trim();
//			String roomEnd = matcher.group(3);
//
//			String roomNumber = roomStart != null ? roomStart : roomEnd;
//
//			System.out.println("Room: " + (roomNumber != null ? roomNumber : "not provided"));
//			System.out.println("Feedback: " + message);
//
//			return handleFeedback(roomNumber, guestMessage);
//		}
		
		Matcher intentMatcher = patternIsFeedback.matcher(guestMessage);
		if (intentMatcher.find()) {
		    Matcher contentMatcher = patternFeedbackWithRoom.matcher(guestMessage);
		    if (contentMatcher.matches()) {
		        String room1 = contentMatcher.group(1);
		        String message = contentMatcher.group(2).trim();
		        String room2 = contentMatcher.group(3);
		        String roomNumber = room1 != null ? room1 : room2;
		        
		        return handleFeedback(roomNumber, guestMessage);
		    }
		}


		// **************************
		// Escalation:
		// **************************

		// Matcher matcherEscalateToHuman = patternEscalateToHuman.matcher(guestMessage);
		// if (matcherEscalateToHuman.find()) {
		//     System.out.println("DEBUG: Matched escalation request.");
		//    return handleEscalationRequest(guestMessage);
		// }

		// **************************
		// Prompt :
		// **************************

		return getAiResponse(guestMessage);
	}	

	private Mono<String> getAiResponse(String guestMessage) {
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

							return room.map(r -> "Room Information: " + r.getDescription() + " Price: $" + r.getPrice() + " Amenities: " + r.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | \n")
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

		// The AI model call and its associated try-catch block for the final response

		// and feedback extraction from AI response are moved here directly.
		try {
			String aiResponseContent = chatModel.call(prompt).getResult().getOutput().getText();
			System.out.println("DEBUG: aiResponseContent: " + aiResponseContent);
			return Mono.just(aiResponseContent); // Return the raw AI response if no feedback JSON is expected
		} catch (Exception e) {
			System.err.println("Error processing AI response: " + e.getMessage());
			return Mono.just("I'm sorry, I encountered an error while processing your request. Please try again later.");
		}
	}

	private Mono<String> handleRoomServiceRequest(String items, String dateContext, String timeStr, String roomNumber, String originalMessage) {
		System.out.println("handleRoomServiceRequest");

		LocalDateTime schdDateTime = null;
		if (timeStr != null && !timeStr.isEmpty()) {
			LocalDate requestDate = LocalDate.now();
			// Default to today

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
						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
						// Example format
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
		return getRoomNumberContext(roomNumber)
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
			LocalDate requestDate = LocalDate.now();
			// Default to today

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
						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
						// Example format
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
		return getRoomNumberContext(roomNumber)
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
				.onErrorResume(IllegalArgumentException.class, e 
						-> {
							System.err.println("ERROR: Failed to create housekeeping request: " + e.getMessage());
							return Mono.just("I'm sorry, I couldn't complete your housekeeping request: " + e.getMessage());
						})
				.onErrorResume(Exception.class, e -> {
					System.err.println("CRITICAL ERROR: Unexpected error placing housekeeping request: " + e.getMessage());
					e.printStackTrace();
					return Mono.just("I encountered an unexpected issue while trying to place your housekeeping request. Please try again later.");
				});
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//handleFeedback: DON'T DELETE, THIS HAS THE CORRECT LOGIC
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//private Mono<String> handleFeedback(String roomNumber, String originalMessage) {
	//    Long currentUserId = getCurrentUserId(); // Assuming this method exists to get the current user ID
	//
	//    // Case 1: roomNumber is explicitly provided
	//    if (roomNumber != null && !roomNumber.trim().isEmpty()) {
	//        return getRoomNumberContext(roomNumber)
	//                .flatMap(context -> {
	//                    Feedback savedFeedback = feedbackService.saveFeedback(
	//                            context.user().getId(),           // User ID from context
	//                            context.room().getRoomId(),       // Room ID from context
	//                            Feedback.FeedbackType.GENERAL,    // Assuming general feedback for chat
	//                            originalMessage,                  // The actual message/description
	//                            context.stayId()                  // Stay ID from context (can be null)
	//                    );
	//                    return Mono.just("Your feedback for room " + roomNumber +
	//                            " has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".");
	//                })
	//                .onErrorResume(IllegalArgumentException.class, e -> {
	//                    System.err.println("ERROR: Failed to submit feedback for a specific room: " + e.getMessage());
	//                    return Mono.just("I'm sorry, I couldn't submit your feedback for that room: " + e.getMessage());
	//                })
	//                .onErrorResume(Exception.class, e -> {
	//                    System.err.println("CRITICAL ERROR: Unexpected error submitting feedback for specific room: " + e.getMessage());
	//                    e.printStackTrace();
	//                    return Mono.just("I encountered an unexpected issue while trying to submit your feedback. Please try again later.");
	//                });
	//    } else {
	//        // Case 2: roomNumber is NOT provided, try to infer from active stay
	//        return Mono.fromCallable(() -> {
	//            User currentUser = userRepository.findById(currentUserId)
	//                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + currentUserId + " not found."));
	//
	//            Long inferredRoomId = null;
	//            Long inferredStayId = null;
	//            String inferredRoomNumber = null;
	//
	//            // Attempt to find an active stay for the user
	//            List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);
	//
	//            if (!activeStays.isEmpty()) {
	//                // If there's exactly one active stay, use its room and stay ID
	//                if (activeStays.size() == 1) {
	//                    Stay activeStay = activeStays.get(0);
	//                    if (activeStay.getRoom() != null) {
	//                        inferredRoomId = activeStay.getRoom().getRoomId();
	//                        inferredRoomNumber = activeStay.getRoom().getRoomNumber();
	//                    }
	//                    inferredStayId = activeStay.getStayId();
	//                    System.out.println("DEBUG: Inferred room " + inferredRoomNumber + " and stay " + inferredStayId + " from user's single active stay.");
	//                } else {
	//                    // Handle ambiguity if multiple active stays
	//                    System.err.println("WARNING: User " + currentUserId + " has multiple active stays. Cannot infer specific room for feedback.");
	//                    // Optionally, you might want to return an error to the user asking them to specify
	//                    // For now, we proceed without room/stay if ambiguous.
	//                }
	//            } else {
	//                System.out.println("DEBUG: User " + currentUserId + " has no active stays. Submitting general feedback without room/stay context.");
	//            }
	//
	//            Feedback savedFeedback = feedbackService.saveFeedback(
	//                    currentUser.getId(),        // User ID
	//                    inferredRoomId,             // Inferred Room ID (can be null)
	//                    Feedback.FeedbackType.GENERAL,
	//                    originalMessage,
	//                    inferredStayId              // Inferred Stay ID (can be null)
	//            );
	//
	//            // Construct response based on whether a room was inferred
	//            if (inferredRoomNumber != null) {
	//                return "Your feedback for your current room " + inferredRoomNumber +
	//                       " has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".";
	//            } else {
	//                return "Your general feedback has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".";
	//            }
	//        })
	//        .onErrorResume(IllegalArgumentException.class, e -> {
	//            System.err.println("ERROR: Failed to submit general feedback: " + e.getMessage());
	//            return Mono.just("I'm sorry, I couldn't submit your feedback: " + e.getMessage());
	//        })
	//        .onErrorResume(Exception.class, e -> {
	//            System.err.println("CRITICAL ERROR: Unexpected error submitting general feedback: " + e.getMessage());
	//            e.printStackTrace();
	//            return Mono.just("I encountered an unexpected issue while trying to submit your feedback. Please try again later.");
	//        });
	//    }
	//}


	//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	handleFeedback: TESTING PURPOSES
	//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private Mono<String> handleFeedback(String roomNumber, String originalMessage) {
		Long currentUserId = getCurrentUserId(); // Assuming this method exists to get the current user ID

		// Case 1: roomNumber is explicitly provided
		if (roomNumber != null && !roomNumber.trim().isEmpty()) {
			return getRoomNumberContext(roomNumber)
					.flatMap(context -> {
						Feedback savedFeedback = feedbackService.saveFeedback(
								context.user().getId(),           // User ID from context
								context.room().getRoomId(),       // Room ID from context
								Feedback.FeedbackType.GENERAL,    // Assuming general feedback for chat
								originalMessage,                  // The actual message/description
								context.stayId()                  // Stay ID from context (can be null)
								);
						return Mono.just("Your feedback for room " + roomNumber +
								" has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".");
					})
					.onErrorResume(IllegalArgumentException.class, e -> {
						System.err.println("ERROR: Failed to submit feedback for a specific room: " + e.getMessage());
						return Mono.just("I'm sorry, I couldn't submit your feedback for that room: " + e.getMessage());
					})
					.onErrorResume(Exception.class, e -> {
						System.err.println("CRITICAL ERROR: Unexpected error submitting feedback for specific room: " + e.getMessage());
						e.printStackTrace();
						return Mono.just("I encountered an unexpected issue while trying to submit your feedback. Please try again later.");
					});
		} else {
			// Case 2: roomNumber is NOT provided, try to infer from active stay
			return Mono.fromCallable(() -> {
				User currentUser = userRepository.findById(currentUserId)
						.orElseThrow(() -> new IllegalArgumentException("User with ID " + currentUserId + " not found."));

				Long inferredRoomId = null;
				Long inferredStayId = null;
				String inferredRoomNumber = null;

				// Attempt to find an active stay for the user
				List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS);

				if (!activeStays.isEmpty()) {
					// If there's one or more active stays, use the first one
					Stay activeStay = activeStays.get(0); // *** Changed: Always get the first stay ***
					if (activeStay.getRoom() != null) {
						inferredRoomId = activeStay.getRoom().getRoomId();
						inferredRoomNumber = activeStay.getRoom().getRoomNumber();
					}
					inferredStayId = activeStay.getStayId();

					if (activeStays.size() > 1) {
						System.out.println("WARNING: User " + currentUserId + " has multiple active stays. Using the first found stay (ID: " + inferredStayId + ", Room: " + inferredRoomNumber + ") for feedback context.");
					} else {
						System.out.println("DEBUG: Inferred room " + inferredRoomNumber + " and stay " + inferredStayId + " from user's single active stay.");
					}
				} else {
					System.out.println("DEBUG: User " + currentUserId + " has no active stays. Submitting general feedback without room/stay context.");
				}

				Feedback savedFeedback = feedbackService.saveFeedback(
						currentUser.getId(),        // User ID
						inferredRoomId,             // Inferred Room ID (can be null if room is null)
						Feedback.FeedbackType.GENERAL,
						originalMessage,
						inferredStayId              // Inferred Stay ID (can be null)
						);

				// Construct response based on whether a room was inferred
				if (inferredRoomNumber != null) {
					return "Your feedback for your current room " + inferredRoomNumber +
							" has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".";
				} else {
					return "Your general feedback has been submitted. Thank you for your input! Feedback ID: " + savedFeedback.getFeedbackId() + ".";
				}
			})
					.onErrorResume(IllegalArgumentException.class, e -> {
						System.err.println("ERROR: Failed to submit general feedback: " + e.getMessage());
						return Mono.just("I'm sorry, I couldn't submit your feedback: " + e.getMessage());
					})
					.onErrorResume(Exception.class, e -> {
						System.err.println("CRITICAL ERROR: Unexpected error submitting general feedback: " + e.getMessage());
						e.printStackTrace();
						return Mono.just("I encountered an unexpected issue while trying to submit your feedback. Please try again later.");
					});
		}
	}

	// New private record to hold the common context for room/stay related requests
	private record RoomNumbertContext(User user, Room room, Long stayId) {}


	// New helper method to get the common Room and Stay context
	private Mono<RoomNumbertContext> getRoomNumberContext(String roomNumber) {
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

			return Mono.just(new RoomNumbertContext(currentUser, targetRoom, stayId));

		} catch (IllegalArgumentException e) {
			return Mono.error(e);
			// Propagate known IllegalArgumentExceptions
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

	// Helper method to parse date strings
	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			return LocalDate.parse(dateStr, formatter);
		} catch (DateTimeParseException e) {
			System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
			return null;
		}
	}

	// New processBooking method moved from ChatController

	public Mono<Booking> processBooking(String username, String hotelName, String roomType, Integer numberOfGuests, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
		return Mono.fromCallable(() -> {
			Booking booked = bookingService.createBooking(
					username,
					hotelName,
					roomType,
					numberOfGuests,
					checkInDate,
					checkOutDate,
					numberOfRooms
					);
			System.out.println("Booking successfully created with ID: " + booked.getBookingId());
			return booked;
		});
	}

	private Mono<String> processBooking(Long userId, String hotelName, String roomType, Integer numberOfGuests, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
		return Mono.fromCallable(() -> {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

			Booking booked = bookingService.createBooking(
					user.getUsername(), // Assuming createBooking takes username
					hotelName,
					roomType,
					numberOfGuests,
					checkInDate,
					checkOutDate,
					numberOfRooms
					);
			System.out.println("Booking successfully created with ID: " + booked.getBookingId());
			return "Your reservation has been successfully made! Booking ID: " + booked.getBookingId();
		}).onErrorResume(IllegalArgumentException.class, e -> {
			System.err.println("Error creating booking: " + e.getMessage());
			return Mono.just("I apologize, but I encountered an error trying to complete your reservation: " + e.getMessage());
		}).onErrorResume(IllegalStateException.class, e -> {
			System.err.println("Unexpected state during booking: " + e.getMessage());
			return Mono.error(new IllegalStateException("An unexpected error occurred while processing your request context."));
		});
	}


}


//package com.hotel.chatbox.service;
//
//import com.hotel.chatbox.domain.Amenities;
//import com.hotel.chatbox.domain.Feedback;
//import com.hotel.chatbox.domain.Hotel;
//import com.hotel.chatbox.domain.HotelRoom;
//import com.hotel.chatbox.domain.Room;
//import com.hotel.chatbox.domain.ServiceRequest;
//import com.hotel.chatbox.domain.Stay;
//import com.hotel.chatbox.domain.User;
//import com.hotel.chatbox.repository.AmenitiesRepository;
//import com.hotel.chatbox.repository.BookingRepository;
//import com.hotel.chatbox.repository.FeedbackRepository;
//import com.hotel.chatbox.repository.HotelRepository;
//import com.hotel.chatbox.repository.HotelRoomRepository;
//import com.hotel.chatbox.repository.RoomRepository;
//import com.hotel.chatbox.repository.RoomReservationRepository;
//import com.hotel.chatbox.repository.StayRepository;
//import com.hotel.chatbox.repository.UserRepository;
//import com.hotel.chatbox.repository.FacilityReservationRepository;
//
//import com.hotel.chatbox.model.requests.ToolRequests.CheckInRequest;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.ai.vectorstore.SearchRequest;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import org.springframework.ai.document.Document;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.MessageSource;
//import org.springframework.context.i18n.LocaleContextHolder;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import reactor.core.publisher.Mono;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import java.util.function.Function;
//import java.util.Locale;
//
//
//@Service
//public class ChatService {
//	@Value("classpath:/prompts/chat-prompt.st")
//	private Resource chatPromptTemplate;
//
//	// *****************************************
//	// CHECK IN
//	// *****************************************
//
//	// Regex Patterns - Defined once for efficiency
//	private final Pattern patternCheckIn = Pattern.compile("check[- ]?in.*booking\\s*id\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
//	// *****************************************
//	// ROOM SERVICE
//	// *****************************************
//
//	// Pattern 1: Full room service request - items, room, and optional time/date
//	// This pattern now specifically looks for "order," "get," or "send up" (for items)
//	// and implicitly excludes "housekeeping."
//	private final Pattern patternRoomServiceFull = Pattern.compile(
//			"(?i)" +
//					"^(?!.*housekeeping)" +                                  // ⛔ Negative lookahead to exclude housekeeping
//					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|request)?\\s*(?:a|an|some)?\\s*" +
//					"([a-zA-Z\\s]+?)\\s*" +                                   // Group 1: items
//					"for room\\s*([Ss]?\\d+)\\s*" +
//					"(?:for\\s*(today|tomorrow)\\s*)?" +                      // Group 3: date context
//					"(?:by|at)?\\s*[‘'\"\\u2018\\u2019]?\\s*" +               // optional left quote
//					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)\\s*" +              // Group 4: time
//					"[’'\"]?"
//			);
//	// Pattern 2: Room service with items and time (assuming current user's room, or system will ask)
//	// This pattern also focuses on verbs related to getting items and excludes "housekeeping."
//	private final Pattern patternRoomServiceItemsTime = Pattern.compile(
//			"(?i)" +
//					"^(?!.*\\bhousekeeping\\b).*" +  // Exclude if 'housekeeping' is present
//					"(?:I'd like to|Can I|Please)?\\s*(?:order|get|send(?: up)?)?\\s*" +
//					"([a-zA-Z\\s]+?)\\s*" +                         // Group 1: items
//					"(?:delivered to my room)?\\s*(?:by|at)\\s*" +
//					"(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)" // Group 2: time
//			);
//	// Pattern 3: Room service with items only (will require prompt for room/time)
//	// Ensures "room service" is mentioned or the verbs clearly imply ordering.
//	private final Pattern patternRoomServiceItemsOnly = Pattern.compile(
//			"(?i)" +
//					"^(?!.*\\bhousekeeping\\b).*" +  // Still exclude 'housekeeping'
//					"(?:I'd like to|Can I)?\\s*(?:order|get)\\s+" +
//					"([a-zA-Z\\s]+?)(?:\\s*for room service)?"
//			);
//	// *****************************************
//	// HOUSEKEEPING
//	// *****************************************
//
//	// Pattern 1: Full housekeeping request - room and optional time/date
//	// This pattern is designed to clearly capture the room number and the full time phrase.
//	private final Pattern patternHousekeepingFull = Pattern.compile(
//			"(?:(?:I'd like to|Can I|get|Please)\\s*(?:schedule|get)?(?:\\s*a)?\\s*housekeeping(?: service)?)\\s+" + // Extended verbs
//					"for room\\s*([Ss]?\\d+)" + // Group 1: Room number
//					"(?:\\s+(?:for|by|at)\\s*(tomorrow|today)?(?:\\s*(?:by|at)?)?\\s*[‘'\"\\u2018\\u2019]?(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)[’'\"]?)?", // Groups 2 & 3: date & time with quote tolerance
//					Pattern.CASE_INSENSITIVE
//			);
//	// Pattern 2: Housekeeping with Time, but no explicit room (system would then ask for room)
//	private final Pattern patternHousekeepingTimeOnly = Pattern.compile(
//			"(?:(?:I'd like to|Can I|Please) (?:schedule)?(?:a)?\\s*housekeeping(?: service)?)\\s+" +
//					"(?:for\\s*(tomorrow|today)?\\s*(?:by|at)?)?\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?)", // Group 1: tomorrow/today, Group 2: Time
//					Pattern.CASE_INSENSITIVE
//			);
//	// Pattern 3: Simple request for housekeeping (system would then ask for room and time)
//	private final Pattern patternHousekeepingSimple = Pattern.compile(
//			"(?:(?:I need|I'd like|Can I get|Please send) (?:a)?\\s*housekeeping(?: service)?)",
//			Pattern.CASE_INSENSITIVE
//			);
//	// *****************************************
//	// FEEDBACK
//	// *****************************************
//
//	private final Pattern patternFeedbackWithRoom = Pattern.compile(
//		    "(?i)" +
//		    "(?:room\\s*([Ss]?\\d+)\\s*[:,\\-]?\\s*)?(.*?)(?:\\s+room\\s*([Ss]?\\d+)\\b)?",
//		    Pattern.DOTALL
//		);
//	// This pattern will now primarily focus on identifying if the message is a feedback
//	private final Pattern patternIsFeedback = Pattern.compile(
//		    "(?:(?:I'd like to|Can I|get|Please)\\s*(?:schedule|get)?(?:\\s*a)?\\s*feedback(?: service)?|" + // First main alternative
//		    "(?:I need|I'd like|Can I get|Please send)\\s*(?:a)?\\s*feedback(?: service)?)",                 // Second main alternative
//		    Pattern.CASE_INSENSITIVE
//		);
//	// *****************************************
//	// MANUAL ESCALATION
//	// *****************************************
//
//	private final Pattern patternEscalateToHuman = Pattern.compile(
//			"(?:I want to talk to|connect me with|speak to|I need to speak with|escalate to|transfer me to)\\s*(?:a|the)?\\s*(?:human|agent|representative|manager|someone|person)",
//			Pattern.CASE_INSENSITIVE
//			);
//	private final ChatModel chatModel;
//	private final VectorStore vectorStore;
//	private final HotelRepository hotelRepository;
//	private final HotelRoomRepository hotelRoomRepository;
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
//			HotelRoomRepository hotelRoomRepository,
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
//		this.hotelRoomRepository = hotelRoomRepository;
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
//	public Mono<String> handleGuestQuery(String guestMessage /* , Locale userLocale */) {
//		Locale userLocale = LocaleContextHolder.getLocale();
//
//		System.out.println("guestMessage -1: " + guestMessage);
//		System.out.println("User Locale: " + userLocale);
//
//
//	    // **************************
//	    // Human Agent Escalation Start:
//	    // **************************
//
//	    Matcher matcherEscalateToHuman = patternEscalateToHuman.matcher(guestMessage);
////		if (matcherEscalateToHuman.find()) {
////	        System.out.println("DEBUG: Matched Human Agent Escalation request.");
////	        // Use messageSource for the response 
////	        try {
////	            Long currentUserId = getCurrentUserId(); // 
////	            User user = userRepository.findById(currentUserId) // 
////	                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_escalation", null, userLocale))); // Added specific message key 
////
////	            // Create a feedback entry of type ESCALATION 
////	            Feedback feedback = new Feedback(
////	                null, // ID (auto-generated) 
////	                user, // 
////	                null, // No specific hotel for general escalation 
////	                null, // No specific booking for general escalation 
////	                Feedback.FeedbackType.COMPLAINT, // Changed to ESCALATION 
////	                "User requested to speak with a human: \"" + guestMessage + "\"", // Always log original message 
////	                LocalDateTime.now(), // 
////	                Feedback.FeedbackStatus.NEW, // Initial status 
////	                "Awaiting human agent assignment." // Initial agent notes 
////	            );
////	            feedbackRepository.save(feedback); // 
////	            System.out.println("DEBUG: Escalation request logged for user " + user.getUsername() + ". Feedback ID: " + feedback.getFeedbackId()); // 
////
////	            return Mono.just(messageSource.getMessage("human_escalation.success", null, userLocale)); // 
////
////	        } catch (IllegalStateException | IllegalArgumentException e) {
////	            System.err.println("ERROR: Failed to log escalation request due to user context: " + e.getMessage()); // 
////	            return Mono.just(messageSource.getMessage("human_escalation.error", null, userLocale)); // 
////	        } catch (Exception e) {
////	            System.err.println("CRITICAL ERROR: Unexpected error during human escalation: " + e.getMessage()); // 
////	            e.printStackTrace(); // 
////	            return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale)); // 
////	        }
////	    }
//
//	    // **************************
//	    // Check In:
//	    // **************************
//
//	    Matcher matcherCheckIn = patternCheckIn.matcher(guestMessage); // 
//		if (matcherCheckIn.find()) { // 
//	        Long bookingId = Long.parseLong(matcherCheckIn.group(1)); // 
//	        var checkInFunction = (Function<CheckInRequest, String>) applicationContext.getBean("performCheckIn"); // 
//	        String result = checkInFunction.apply(new CheckInRequest(bookingId)); // 
//	        // Assuming 'performCheckIn' returns a simple status, you might want to translate it here
//	        if (result.contains("confirmed")) { // Simple check, adapt to your actual return value
//	            return Mono.just(messageSource.getMessage("checkin.success", new Object[]{bookingId}, userLocale));
//	        } else {
//	            // Use a more specific error message if available, otherwise a generic one
//	            return Mono.just(messageSource.getMessage("checkin.error.not_found", new Object[]{bookingId}, userLocale));
//	        }
//	    }
//
//	    // **************************
//	    // Room Service:
//	    // **************************
//
//	    Matcher matcherRoomServiceFull = patternRoomServiceFull.matcher(guestMessage); // 
//	    Matcher matcherRoomServiceItemsTime = patternRoomServiceItemsTime.matcher(guestMessage); // 
//	    Matcher matcherRoomServiceItemsOnly = patternRoomServiceItemsOnly.matcher(guestMessage); // 
//
//	    if (matcherRoomServiceFull.find()) {
//	        String items = matcherRoomServiceFull.group(1).trim(); // 
//	        String roomNumber = matcherRoomServiceFull.group(2).trim(); // 
//	        String dateContext = matcherRoomServiceFull.group(3); // Group 3: dateContext 
//	        String timeStr = matcherRoomServiceFull.group(4); // Group 4: timeStr 
//
//	        System.out.println("DEBUG RoomService - Full Match:"); // 
//	        System.out.println("  Items: " + items); // 
//	        System.out.println("  Room: " + roomNumber); // 
//	        System.out.println("  Date: " + dateContext); // 
//	        System.out.println("  Time: " + timeStr); // 
//
//	        return handleRoomServiceRequest(items, dateContext, timeStr, roomNumber, guestMessage, userLocale); // Pass userLocale 
//	    } else if (matcherRoomServiceItemsTime.find()) { // 
//	        String items = matcherRoomServiceItemsTime.group(1).trim(); // 
//	        String timeStr = matcherRoomServiceItemsTime.group(2).trim(); // Group 2: timeStr (corrected from group 3) 
//          String dateContext = matcherRoomServiceItemsTime.group(3); // Group 3: dateContext (corrected from group 2 if applicable, check regex)  - *Note: The provided regex has 2 groups. Group 1 is items, Group 2 is time.*
//          // Re-evaluating based on source 207:
//          // Group 1: ([a-zA-Z\\s]+?)  -> items
//          // Group 2: (\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?(?: tonight| this morning)?) -> time
//          // So, dateContext would be part of timeStr for this pattern.
//          dateContext = null; // Reset dateContext for this pattern if it's integrated into timeStr
//          // Let's adjust to reflect the actual regex groups.
//
//	        System.out.println("DEBUG RoomService - Matched items+time:"); // 
//	        System.out.println("  Items: " + items); // 
//	        System.out.println("  Time: " + timeStr); // 
//	        String responseTime = timeStr; // 
//	        return Mono.just(messageSource.getMessage("room_service.ask_room", new Object[]{items, responseTime}, userLocale)); // 
//	    } else if (matcherRoomServiceItemsOnly.find()) { // 
//	        String items = matcherRoomServiceItemsOnly.group(1).trim(); // 
//	        System.out.println("DEBUG RoomService - Matched items only:"); // 
//	        System.out.println("  Items: " + items); // 
//	        return Mono.just(messageSource.getMessage("room_service.ask_room_time", new Object[]{items}, userLocale)); // 
//	    }
//
//	    // **************************
//	    // Housekeeping Service:
//	    // **************************
//
//	    Matcher matcherHousekeepingFull = patternHousekeepingFull.matcher(guestMessage); // 
//	    Matcher matcherHousekeepingTimeOnly = patternHousekeepingTimeOnly.matcher(guestMessage); // 
//	    Matcher matcherHousekeepingSimple = patternHousekeepingSimple.matcher(guestMessage); // 
//
//	    if (matcherHousekeepingFull.find()) {
//	        String roomNumber = matcherHousekeepingFull.group(1); // Group 1: roomNumber 
//	        String dateContext = matcherHousekeepingFull.group(2); // Group 2: dateContext 
//	        String timeStr = matcherHousekeepingFull.group(3); // Group 3: timeStr 
//
//	        System.out.println("DEBUG Housekeeping - Matched Full Pattern:"); // 
//	        System.out.println("  Room Number: " + roomNumber); // 
//	        System.out.println("  Date Context (Group 2): " + dateContext); // 
//	        System.out.println("  Time String (Group 3): " + timeStr); // 
//
//	        return handleHousekeepingRequest(dateContext, timeStr, roomNumber, guestMessage, userLocale); // Pass userLocale 
//	    } else if (matcherHousekeepingTimeOnly.find()) { // 
//	        String dateContext = matcherHousekeepingTimeOnly.group(1); // Group 1: dateContext 
//	        String timeStr = matcherHousekeepingTimeOnly.group(2).trim(); // Group 2: timeStr 
//
//	        System.out.println("DEBUG Housekeeping - Matched Time Only Pattern:"); // 
//	        System.out.println("  Date Context (Group 1): " + dateContext); // 
//	        System.out.println("  Time String (Group 2): " + timeStr); // 
//	        String responseTime = (dateContext != null ? dateContext + " " : "") + timeStr; // 
//	        return Mono.just(messageSource.getMessage("housekeeping.ask_room", new Object[]{responseTime}, userLocale)); // 
//	    } else if (matcherHousekeepingSimple.find()) { // 
//	        System.out.println("DEBUG Housekeeping - Matched Simple Pattern."); // 
//	        return Mono.just(messageSource.getMessage("housekeeping.ask_room_time", null, userLocale)); // 
//	    }
//
//
//	    // **************************
//	    // Feedback:
//	    // **************************
//
//	    Matcher matcher = patternFeedbackWithRoom.matcher(guestMessage); // 
//		if (matcher.matches()) { // 
//	        String roomStart = matcher.group(1); // 
//	        String message = matcher.group(2).trim(); // 
//	        String roomEnd = matcher.group(3); // 
//
//	        String roomNumber = roomStart != null ? roomStart : roomEnd; // 
//	        System.out.println("Room: " + (roomNumber != null ? roomNumber : "not provided")); // 
//	        System.out.println("Feedback: " + message); // 
//
//	        return handleFeedback(roomNumber, guestMessage, userLocale); // Pass userLocale 
//	    }
//
//	    // **************************
//	    // Prompt :
//	    // **************************
//
//	    List<Document> relevantDocuments = vectorStore.similaritySearch(
//	            SearchRequest.builder().query(guestMessage).topK(3).build()); // 
//	    String context = relevantDocuments.stream() // 
//	            .map(doc -> { // 
//	                String currentContent = doc.getFormattedContent(); // 
//	                String type = (String) doc.getMetadata().get("type"); // 
//	                Integer sourceId = (Integer) doc.getMetadata().get("source_id"); // 
//
//	                if (sourceId != null) { // 
//	                    if ("hotel_description".equals(type)) { // 
//	                        Optional<Hotel> hotel = hotelRepository.findById(sourceId); // 
//	                        return hotel.map(h -> "Hotel Information: " + h.getHotelName() + " - " + h.getDescription() + " Amenities: " + h.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | ") // 
//	                                .orElse(currentContent); // 
//	                    } else if ("room_description".equals(type)) { // 
//	                        Optional<HotelRoom> room = hotelRoomRepository.findById(sourceId); // 
//	                        return room.map(r -> "Room Information: " + r.getDescription() + " Price: $" + r.getPrice() + " Amenities: " + r.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + " | \n") // 
//	                                .orElse(currentContent); // 
//	                    } else if ("amenity_info".equals(type)) { // 
//	                        Optional<Amenities> amenity = amenitiesRepository.findById(sourceId); // 
//	                        return amenity.map(a -> "Amenity: " + a.getName() + " | ") // 
//	                                .orElse(currentContent); // 
//	                    }
//	                }
//	                return currentContent; // 
//	            })
//	            .collect(Collectors.joining("\n---\n")); // 
//
//	    PromptTemplate promptTemplate = null;
//		try {
//			promptTemplate = new PromptTemplate(chatPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} // Use .getContentAsString() for Resource 
//	    Prompt prompt = promptTemplate.create(
//	            Map.of("context", context, "question", guestMessage, "locale", userLocale.toLanguageTag()) // Pass locale to prompt 
//	            );
//
//	    System.out.println("DEBUG: Sending prompt to AI ChatModel: " + prompt.getContents()); // 
//
//	    // The AI model call and its associated try-catch block for the final response 
//	    // and feedback extraction from AI response are moved here directly. 
//	    try {
//	        String aiResponseContent = chatModel.call(prompt).getResult().getOutput().getText(); // 
//	        System.out.println("DEBUG: aiResponseContent: " + aiResponseContent); // 
//	        return Mono.just(aiResponseContent); // Return the raw AI response if no feedback JSON is expected 
//	    } catch (Exception e) {
//	        System.err.println("Error processing AI response: " + e.getMessage()); // 
//	        return Mono.just(messageSource.getMessage("error.unexpected_ai_response", null, userLocale)); // Use messageSource for AI errors 
//	    }
//	}
//
//
//	private Mono<String> handleRoomServiceRequest(String items, String dateContext, String timeStr, String roomNumber, String originalMessage, Locale userLocale) { // Added userLocale
//		System.out.println("handleRoomServiceRequest"); // 
//
//		LocalDateTime schdDateTime = null; // 
//		if (timeStr != null && !timeStr.isEmpty()) { // 
//			LocalDate requestDate = LocalDate.now(); // Default to today 
//
//			// Use the new dateContext parameter 
//			if (dateContext != null) { // 
//				if (dateContext.equalsIgnoreCase("today")) {
//					requestDate = LocalDate.now(); // Corrected: should be today, not plusDays(1) 
//				} else if (dateContext.equalsIgnoreCase("tomorrow")) { // 
//					requestDate = LocalDate.now().plusDays(1); // 
//				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) { // 
//					requestDate = LocalDate.now().plusDays(2); // 
//				}
//				else {
//					try {
//						// Define a formatter that can parse your date string formats 
//						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", userLocale); // Example format, using locale 
//						requestDate = LocalDate.parse(dateContext, dateFormatter); // 
//					} catch (DateTimeParseException e) {
//						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage()); // 
//						// Fallback to today if parsing fails for explicit dates 
//					}
//				}
//			}
//
//			try {
//				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")); // 
//				schdDateTime = LocalDateTime.of(requestDate, parsedTime); // 
//			} catch (DateTimeParseException e) { // 
//				try {
//					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a", userLocale)); // Using locale for AM/PM 
//					schdDateTime = LocalDateTime.of(requestDate, parsedTime); // 
//				} catch (DateTimeParseException e2) { // 
//					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage()); // 
//					return Mono.just(messageSource.getMessage("error.parse_time", null, userLocale)); // Using messageSource 
//				}
//			}
//		}
//
//		LocalDateTime scheduledDateTime = schdDateTime; // 
//		return getRoomNumberContext(roomNumber, userLocale) // Pass userLocale
//				.flatMap(context -> {
//					ServiceRequest roomServiceReq = serviceRequestService.createRoomServiceRequest(
//							context.user().getId(), // Use ID from context
//							context.room().getRoomId(),   // Use Room ID from context
//							items,
//							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
//							context.stayId()              // Use Stay ID from context (can be null)
//							);
//					String formattedScheduledTime = (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a", userLocale)) : messageSource.getMessage("now", null, userLocale)); // Use locale for format and "now"
//					return Mono.just(messageSource.getMessage("room_service.confirm", new Object[]{items, roomNumber, formattedScheduledTime, roomServiceReq.getRequestId()}, userLocale));
//				})
//				.onErrorResume(IllegalArgumentException.class, e -> { // 
//					System.err.println("ERROR: Failed to create room service request: " + e.getMessage()); // 
//					// Customize error messages based on the exception message or type
//					if (e.getMessage().contains("active stays")) {
//                       return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//                  } else if (e.getMessage().contains("room number")) {
//                       return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//                  } else if (e.getMessage().contains("multiple active stays")) {
//                       return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//                  } else if (e.getMessage().contains("User with ID")) {
//                  	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//                  }
//					return Mono.just(messageSource.getMessage("error.unexpected_room_service", new Object[]{e.getMessage()}, userLocale)); // More generic fallback 
//				})
//				.onErrorResume(Exception.class, e -> { // 
//					System.err.println("CRITICAL ERROR: Unexpected error placing room service: " + e.getMessage()); // 
//					e.printStackTrace(); // 
//					return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale)); // 
//				});
//	}
//
//	private Mono<String> handleHousekeepingRequest(String dateContext, String timeStr, String roomNumber, String originalMessage, Locale userLocale) { // Added userLocale
//		LocalDateTime schdDateTime = null; // 
//		if (timeStr != null && !timeStr.isEmpty()) { // 
//			LocalDate requestDate = LocalDate.now(); // Default to today 
//
//			// Use the new dateContext parameter 
//			if (dateContext != null) { // 
//				if (dateContext.equalsIgnoreCase("today")) {
//					requestDate = LocalDate.now(); // Corrected: should be today, not plusDays(1) 
//				} else if (dateContext.equalsIgnoreCase("tomorrow")) { // 
//					requestDate = LocalDate.now().plusDays(1); // 
//				} else if (dateContext.equalsIgnoreCase("day after tomorrow")) { // 
//					requestDate = LocalDate.now().plusDays(2); // 
//				}
//				else {
//					try {
//						// Define a formatter that can parse your date string formats 
//						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", userLocale); // Example format, using locale 
//						requestDate = LocalDate.parse(dateContext, dateFormatter); // 
//					} catch (DateTimeParseException e) { // 
//						System.err.println("WARNING: Could not parse explicit date from dateContext '" + dateContext + "': " + e.getMessage()); // 
//						// Fallback to today if parsing fails for explicit dates 
//					}
//				}
//			}
//
//			try {
//				LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")); // 
//				schdDateTime = LocalDateTime.of(requestDate, parsedTime); // 
//			} catch (DateTimeParseException e) { // 
//				try {
//					LocalTime parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a", userLocale)); // Using locale for AM/PM 
//					schdDateTime = LocalDateTime.of(requestDate, parsedTime); // 
//				} catch (DateTimeParseException e2) { // 
//					System.err.println("ERROR: Could not parse time string '" + timeStr + "': " + e2.getMessage()); // 
//					return Mono.just(messageSource.getMessage("error.parse_time", null, userLocale)); // Using messageSource 
//				}
//			}
//		}
//
//		LocalDateTime scheduledDateTime = schdDateTime; // 
//		// Use the new helper method to get the room and stay context
//		return getRoomNumberContext(roomNumber, userLocale) // Pass userLocale
//				.flatMap(context -> {
//					ServiceRequest housekeepingReq = serviceRequestService.scheduleHousekeeping(
//							context.user().getId(), // Use ID from context
//							context.room().getRoomId(),   // Use Room ID from context
//							(scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null),
//							context.stayId()              // Use Stay ID from context (can be null)
//							);
//					String formattedScheduledTime = (scheduledDateTime != null ? scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a", userLocale)) : messageSource.getMessage("now", null, userLocale)); // Use locale for format and "now"
//					return Mono.just(messageSource.getMessage("housekeeping.confirm", new Object[]{roomNumber, formattedScheduledTime, housekeepingReq.getRequestId()}, userLocale));
//				})
//				.onErrorResume(IllegalArgumentException.class, e -> { // 
//					System.err.println("ERROR: Failed to create housekeeping request: " + e.getMessage()); // 
//					// Customize error messages based on the exception message or type
//					if (e.getMessage().contains("active stays")) {
//                       return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//                  } else if (e.getMessage().contains("room number")) {
//                       return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//                  } else if (e.getMessage().contains("multiple active stays")) {
//                       return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//                  } else if (e.getMessage().contains("User with ID")) {
//                  	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//                  }
//					return Mono.just(messageSource.getMessage("error.unexpected_housekeeping", new Object[]{e.getMessage()}, userLocale)); // More generic fallback 
//				})
//				.onErrorResume(Exception.class, e -> { // 
//					System.err.println("CRITICAL ERROR: Unexpected error placing housekeeping request: " + e.getMessage()); // 
//					e.printStackTrace(); // 
//					return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale)); // 
//				});
//	}
//
//	//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	//	handleFeedback: TESTING PURPOSES
//	//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	private Mono<String> handleFeedback(String roomNumber, String originalMessage, Locale userLocale) { // Added userLocale
//	    Long currentUserId = getCurrentUserId(); // 
//
//	    // Case 1: roomNumber is explicitly provided 
//	    if (roomNumber != null && !roomNumber.trim().isEmpty()) { // 
//	        return getRoomNumberContext(roomNumber, userLocale) // Pass userLocale
//	                .flatMap(context -> {
//	                    Feedback savedFeedback = feedbackService.saveFeedback( // 
//	                            context.user().getId(),           // User ID from context 
//	                            context.room().getRoomId(),       // Room ID from context 
//	                            Feedback.FeedbackType.GENERAL,    // Assuming general feedback for chat 
//	                            originalMessage,                  // The actual message/description 
//	                            context.stayId()                  // Stay ID from context (can be null) 
//	                    );
//	                    return Mono.just(messageSource.getMessage("feedback.room_submitted", new Object[]{roomNumber, savedFeedback.getFeedbackId()}, userLocale)); // Using messageSource 
//	                })
//	                .onErrorResume(IllegalArgumentException.class, e -> { // 
//	                    System.err.println("ERROR: Failed to submit feedback for a specific room: " + e.getMessage()); // 
//	                    // Customize error messages based on the exception message or type
//	                    if (e.getMessage().contains("active stays")) {
//	                         return Mono.just(messageSource.getMessage("error.no_active_stays", null, userLocale));
//	                    } else if (e.getMessage().contains("room number")) {
//	                         return Mono.just(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale));
//	                    } else if (e.getMessage().contains("multiple active stays")) {
//	                         return Mono.just(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale));
//	                    } else if (e.getMessage().contains("User with ID")) {
//	                    	 return Mono.just(messageSource.getMessage("error.user_not_found_context", null, userLocale));
//	                    }
//	                    return Mono.just(messageSource.getMessage("error.unexpected_feedback_room", new Object[]{e.getMessage()}, userLocale)); // 
//	                })
//	                .onErrorResume(Exception.class, e -> { // 
//	                    System.err.println("CRITICAL ERROR: Unexpected error submitting feedback for specific room: " + e.getMessage()); // 
//	                    e.printStackTrace(); // 
//	                    return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale)); // 
//	                });
//	    } else {
//	        // Case 2: roomNumber is NOT provided, try to infer from active stay 
//	        return Mono.fromCallable(() -> { // 
//	            User currentUser = userRepository.findById(currentUserId) // 
//	                    .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_feedback", null, userLocale))); // Using messageSource 
//
//	            Long inferredRoomId = null; // 
//	            Long inferredStayId = null; // 
//	            String inferredRoomNumber = null; // 
//
//	            // Attempt to find an active stay for the user 
//	            List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS); // 
//
//	            if (!activeStays.isEmpty()) { // 
//	                // If there's one or more active stays, use the first one 
//	                Stay activeStay = activeStays.get(0); // *** Changed: Always get the first stay *** 
//	                if (activeStay.getRoom() != null) { // 
//	                    inferredRoomId = activeStay.getRoom().getRoomId(); // 
//	                    inferredRoomNumber = activeStay.getRoom().getRoomNumber(); // 
//	                }
//	                inferredStayId = activeStay.getStayId(); // 
//	                if (activeStays.size() > 1) { // 
//	                    System.out.println("WARNING: User " + currentUserId + " has multiple active stays. Using the first found stay (ID: " + inferredStayId + ", Room: " + inferredRoomNumber + ") for feedback context."); // 
//	                } else {
//	                    System.out.println("DEBUG: Inferred room " + inferredRoomNumber + " and stay " + inferredStayId + " from user's single active stay."); // 
//	                }
//	            } else {
//	                System.out.println("DEBUG: User " + currentUserId + " has no active stays. Submitting general feedback without room/stay context."); // 
//	            }
//
//	            Feedback savedFeedback = feedbackService.saveFeedback( // 
//	                    currentUser.getId(),        // User ID 
//	                    inferredRoomId,             // Inferred Room ID (can be null if room is null) 
//	                    Feedback.FeedbackType.GENERAL, // 
//	                    originalMessage, // 
//	                    inferredStayId              // Inferred Stay ID (can be null) 
//	            );
//	            // Construct response based on whether a room was inferred 
//	            if (inferredRoomNumber != null) { // 
//	                return messageSource.getMessage("feedback.room_inferred_submitted", new Object[]{inferredRoomNumber, savedFeedback.getFeedbackId()}, userLocale); // Using messageSource 
//	            } else {
//	                return messageSource.getMessage("feedback.general_submitted", new Object[]{savedFeedback.getFeedbackId()}, userLocale); // Using messageSource 
//	            }
//	        })
//	        .onErrorResume(IllegalArgumentException.class, e -> { // 
//	            System.err.println("ERROR: Failed to submit general feedback: " + e.getMessage()); // 
//	            return Mono.just(messageSource.getMessage("error.unexpected_feedback_general", new Object[]{e.getMessage()}, userLocale)); // 
//	        })
//	        .onErrorResume(Exception.class, e -> { // 
//	            System.err.println("CRITICAL ERROR: Unexpected error submitting general feedback: " + e.getMessage()); // 
//	            e.printStackTrace(); // 
//	            return Mono.just(messageSource.getMessage("error.unexpected", null, userLocale)); // 
//	        });
//	    }
//	}
//
//	// New private record to hold the common context for room/stay related requests
//	private record RoomNumbertContext(User user, Room room, Long stayId) {} // 
//
//
//	// New helper method to get the common Room and Stay context
//	private Mono<RoomNumbertContext> getRoomNumberContext(String roomNumber, Locale userLocale) { // Added userLocale
//		Long currentUserId = getCurrentUserId(); // 
//		try { // 
//			// 1. Get current user 
//			User currentUser = userRepository.findById(currentUserId) // 
//					.orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.user_not_found_context", new Object[]{currentUserId}, userLocale))); // Using messageSource 
//			// 2. Get list of Stay (IN_PROGRESS) by current user 
//			List<Stay> activeStays = stayService.findStaysByUserAndStayStatus(currentUser, Stay.StayStatus.IN_PROGRESS); // 
//			if (activeStays.isEmpty()) { // 
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.no_active_stays", null, userLocale))); // Using messageSource 
//			}
//
//			// 3. Extract all Room entities from the Stay List 
//			List<Room> roomsForActiveStays = activeStays.stream() // 
//					.map(Stay::getRoom) // 
//					.collect(Collectors.toList()); // 
//			// 4. Get the Room that match roomNumber in the room_id list 
//			List<Room> matchingRooms = roomsForActiveStays.stream() // 
//					.filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber)) // 
//					.collect(Collectors.toList()); // 
//			if (matchingRooms.isEmpty()) { // 
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.room_not_in_stay", new Object[]{roomNumber}, userLocale))); // Using messageSource 
//			}
//
//			if (matchingRooms.size() > 1) { // 
//				System.err.println("WARNING: User " + currentUserId + " has multiple active stays with room number " + roomNumber + ". Ambiguity detected."); // 
//				return Mono.error(new IllegalArgumentException(messageSource.getMessage("error.ambiguous_room", new Object[]{roomNumber}, userLocale))); // Using messageSource 
//			}
//
//			// If we reach here, we have a unique matching room for the user's active stays 
//			Room targetRoom = matchingRooms.get(0); // 
//			// Now, find the specific Stay object that corresponds to this targetRoom 
//			Optional<Stay> foundStay = activeStays.stream() // 
//					.filter(stay -> stay.getRoom().equals(targetRoom)) // 
//					.findFirst(); // 
//			Long stayId = foundStay.map(Stay::getStayId).orElse(null); // 
//			if (stayId == null) { // 
//				System.err.println("ERROR: Matched room " + roomNumber + " but could not find corresponding active stay. Proceeding without stayId for ServiceRequest."); // 
//			}
//
//			return Mono.just(new RoomNumbertContext(currentUser, targetRoom, stayId)); // 
//
//		} catch (IllegalArgumentException e) { // 
//			return Mono.error(e); // Propagate known IllegalArgumentExceptions 
//		} catch (Exception e) { // 
//			System.err.println("CRITICAL ERROR: Unexpected error in getRoomServiceRequestContext: " + e.getMessage()); // 
//			e.printStackTrace(); // 
//			return Mono.error(new IllegalStateException(messageSource.getMessage("error.unexpected_context", null, userLocale))); // Using messageSource 
//		}
//	}
//
//	private Long getCurrentUserId() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 
//		if (authentication == null || !authentication.isAuthenticated()) { // 
//			throw new IllegalStateException("User is not authenticated."); // 
//		}
//
//		Object principal = authentication.getPrincipal(); // 
//		if (principal instanceof UserDetails) { // 
//			// Assuming your UserDetails implementation (e.g., CustomUserDetails)
//			// has a way to get the actual user ID from your database User entity. 
//			// You will likely need to cast or have a method in your UserDetails 
//			// to retrieve the specific ID. 
//			String username = ((UserDetails) principal).getUsername(); // 
//			User user = userRepository.findByUsername(username) // 
//					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username)); // 
//			return user.getId(); // Assuming User entity has getId() 
//		} else if (principal instanceof String) { // 
//			// This might happen if the principal is just the username string 
//			String username = (String) principal; // 
//			User user = userRepository.findByUsername(username) // 
//					.orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + username)); // 
//			return user.getId(); // 
//		} else {
//			throw new IllegalStateException("Unable to retrieve user details from authentication principal."); // 
//		}
//	}
//
//}
