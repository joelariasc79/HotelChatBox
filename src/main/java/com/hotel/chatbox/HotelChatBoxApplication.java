package com.hotel.chatbox;

import com.hotel.chatbox.domain.*;
import com.hotel.chatbox.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@SpringBootApplication
public class HotelChatBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelChatBoxApplication.class, args);
    }

    /**
     * CommandLineRunner to populate initial user data for testing.
     * Creates a default 'user', 'admin', and 20 additional generic users.
     */
    
//    @Bean
//    @Order(1)
//    public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            // Create default 'user' if not exists
//            if (userRepository.findByUsername("user").isEmpty()) {
//                User user = new User();
//                user.setUsername("user");
//                user.setPassword(passwordEncoder.encode("password")); // Encode the password!
//                user.setRole("USER");
//                userRepository.save(user);
//                System.out.println("Created user: user/password");
//            }
//
//            // Create default 'admin' if not exists
//            if (userRepository.findByUsername("admin").isEmpty()) {
//                User admin = new User();
//                admin.setUsername("admin");
//                admin.setPassword(passwordEncoder.encode("adminpass")); // Encode the password!
//                admin.setRole("ADMIN");
//                userRepository.save(admin);
//                System.out.println("Created admin: admin/adminpass");
//            }
//            
//         // Add 5 additional generic users
////            for (int i = 1; i <= 5; i++) {
////                String username = "user" + i;
////                if (userRepository.findByUsername(username).isEmpty()) {
////                    User testUser = new User();
////                    testUser.setUsername(username);
////                    testUser.setPassword(passwordEncoder.encode("password" + i)); // Unique password for each
////                    testUser.setRole("USER");
////                    userRepository.save(testUser);
////                    System.out.println("Created user: " + username + "/password" + i);
////                }
////            }
//
//            // Add 20 additional generic users
//            for (int i = 1; i <= 20; i++) {
//                String username = "testuser" + i;
//                if (userRepository.findByUsername(username).isEmpty()) {
//                    User testUser = new User();
//                    testUser.setUsername(username);
//                    testUser.setPassword(passwordEncoder.encode("password" + i)); // Unique password for each
//                    testUser.setRole("USER");
//                    userRepository.save(testUser);
//                    System.out.println("Created user: " + username + "/password" + i);
//                }
//            }
//            System.out.println("Finished populating initial user data.");
//        };
//    }
//
//    /**
//     * CommandLineRunner to initialize data in the database and vector store.
//     * This will run once when the application starts.
//     */
//    @Bean
//    @Order(2)
//    public CommandLineRunner dataInitializer(
//            HotelRepository hotelRepository,
//            RoomTypeRepository roomTypeRepository,
//            AmenitiesRepository amenitiesRepository,
//            HotelRoomRepository hotelRoomRepository,
//            UserRepository userRepository,
//            LoyaltyProgramRepository loyaltyProgramRepository,
//            UserLoyaltyRepository userLoyaltyRepository,
//            UserPreferenceRepository userPreferenceRepository,
//            BookingRepository bookingRepository,
//            FeedbackRepository feedbackRepository,
//            ServiceRequestRepository serviceRequestRepository,
//            FacilityReservationRepository facilityReservationRepository,
//            OfferRepository offerRepository,
//            RoomRepository roomRepository, // Added RoomRepository
//            StayRepository stayRepository, // Added StayRepository
//            VectorStore vectorStore) {
//        return args -> {
//            System.out.println("Initializing data and vector store...");
//            Random random = new Random();
//
//            // --- 1. Create and Save Amenities ---
//            List<Amenities> allAmenities = new ArrayList<>();
//            Amenities parking = new Amenities();
//            parking.setA_id(1);
//            parking.setName("Parking");
//            parking = amenitiesRepository.save(parking);
//            allAmenities.add(parking);
//
//            Amenities wifi = new Amenities();
//            wifi.setA_id(2);
//            wifi.setName("Free Wi-Fi");
//            wifi = amenitiesRepository.save(wifi);
//            allAmenities.add(wifi);
//
//            Amenities pool = new Amenities();
//            pool.setA_id(3);
//            pool.setName("Swimming Pool");
//            pool = amenitiesRepository.save(pool);
//            allAmenities.add(pool);
//            Amenities breakfast = new Amenities();
//            breakfast.setA_id(4);
//            breakfast.setName("Complimentary Breakfast");
//            breakfast = amenitiesRepository.save(breakfast);
//            allAmenities.add(breakfast);
//
//            Amenities fitness = new Amenities();
//            fitness.setA_id(5);
//            fitness.setName("Fitness Center");
//            fitness = amenitiesRepository.save(fitness);
//            allAmenities.add(fitness);
//
//            Amenities bar = new Amenities();
//            bar.setA_id(6);
//            bar.setName("Bar & Lounge");
//            bar = amenitiesRepository.save(bar);
//            allAmenities.add(bar);
//            Amenities spa = new Amenities();
//            spa.setA_id(7);
//            spa.setName("Spa Services");
//            spa = amenitiesRepository.save(spa);
//            allAmenities.add(spa);
//
//            Amenities petsAllowed = new Amenities();
//            petsAllowed.setA_id(8);
//            petsAllowed.setName("Pets Allowed");
//            petsAllowed = amenitiesRepository.save(petsAllowed);
//            allAmenities.add(petsAllowed);
//            
//            Amenities frontDesk24Hour = new Amenities();
//            frontDesk24Hour.setA_id(9);
//            frontDesk24Hour.setName("24-Hour Front Desk");
//            frontDesk24Hour = amenitiesRepository.save(frontDesk24Hour);
//            allAmenities.add(frontDesk24Hour);
//            Amenities restaurant = new Amenities();
//            restaurant.setA_id(10);
//            restaurant.setName("Restaurant");
//            restaurant = amenitiesRepository.save(restaurant);
//            allAmenities.add(restaurant);
//            
//            Amenities conciergeService = new Amenities();
//            conciergeService.setA_id(11);
//            conciergeService.setName("Concierge Service");
//            conciergeService = amenitiesRepository.save(conciergeService);
//            allAmenities.add(conciergeService);
//            
//            Amenities businessCenter = new Amenities();
//            businessCenter.setA_id(12);
//            businessCenter.setName("Business Center");
//            businessCenter = amenitiesRepository.save(businessCenter);
//            allAmenities.add(businessCenter);
//            Amenities laundryService = new Amenities();
//            laundryService.setA_id(13);
//            laundryService.setName("Laundry Service");
//            laundryService = amenitiesRepository.save(laundryService);
//            allAmenities.add(laundryService);
//            
//            Amenities roomService = new Amenities();
//            roomService.setA_id(14);
//            roomService.setName("Room Service");
//            roomService = amenitiesRepository.save(roomService);
//            allAmenities.add(roomService);
//            
//            Amenities familyRooms = new Amenities();
//            familyRooms.setA_id(15);
//            familyRooms.setName("Family Rooms");
//            familyRooms = amenitiesRepository.save(familyRooms);
//            allAmenities.add(familyRooms);
//            
//            System.out.println("Amenities data populated.");
//            // --- 2. Create and Save Room Types ---  
//            List<RoomType> allRoomTypes = new ArrayList<>();
//            RoomType standard = new RoomType();
//            standard.setTypeId(1);
//            standard.setName("Standard");
//            standard = roomTypeRepository.save(standard);
//            allRoomTypes.add(standard);
//
//            RoomType deluxe = new RoomType();
//            deluxe.setTypeId(2);
//            deluxe.setName("Deluxe");
//            deluxe = roomTypeRepository.save(deluxe);
//            allRoomTypes.add(deluxe);
//
//            RoomType suite = new RoomType();
//            suite.setTypeId(3);
//            suite.setName("Suite");
//            suite = roomTypeRepository.save(suite);
//            allRoomTypes.add(suite);
//            
//            System.out.println("Room Types data populated.");
//            // --- 3. Create and Save Hotels ---
//            Hotel hotel1 = new Hotel();
//            hotel1.setHotelName("Grand Plaza Hotel");
//            hotel1.setAddress("123 Main St");
//            hotel1.setCity("New York City");
//            hotel1.setState("New York");
//            hotel1.setStarRating(5);
//            hotel1.setAveragePrice(250.00);
//            hotel1.setDiscount(0.10);
//            hotel1.setDescription("Experience luxury and comfort at our downtown hotel with stunning city views.");
//            hotel1.setEmail("info@grandplaza.com");
//            hotel1.setMobile("123-456-7890");
//            hotel1.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Grand+Plaza");
//            hotel1.setTimesBooked(150);
//            hotel1.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar)));
//            hotel1 = hotelRepository.save(hotel1);
//            Set<Amenities> hotelAmenityNames = new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar));
//            Hotel hotel2 = new Hotel();
//            hotel2.setHotelName("Riverside Inn");
//            hotel2.setAddress("45 River Rd");
//            hotel2.setCity("Austin");
//            hotel2.setState("Texas");
//            hotel2.setStarRating(3);
//            hotel2.setAveragePrice(120.00);
//            hotel2.setDiscount(0.05);
//            hotel2.setDescription("A cozy inn by the river, perfect for a relaxing getaway. Pet-friendly options available.");
//            hotel2.setEmail("contact@riversideinn.com");
//            hotel2.setMobile("987-654-3210");
//            hotel2.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Riverside+Inn");
//            hotel2.setTimesBooked(80);
//            hotel2.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, petsAllowed)));
//            hotel2 = hotelRepository.save(hotel2);
//            
//
//            // --- 3. Create and Save 50 Hotels ---
//            List<Hotel> hotels = new ArrayList<>();
//            String[] commonHotelNames = {"Grand", "Majestic", "Royal", "Downtown", "Riverside", "City Center", "Inn", "Plaza", "Heights", "Bayview"};
//            String[] adjectives = {"Comfort", "Luxury", "Elite", "Prime", "Cornerstone", "Metropolitan", "Harmony", "Azure", "Summit", "Evergreen"};
//            String[] locations = {"Hotel", "Suites", "Lodge", "Resort", "Motel", "Boutique", "Palace"};
//            String[] usCities = {
//                    "New York City, New York", "Los Angeles, California", "Chicago, Illinois",
//                    "Houston, Texas", "Phoenix, Arizona", "Philadelphia, Pennsylvania",
//                    "San Antonio, Texas", "San Diego, California", "Dallas, Texas",
//                    "San Jose, California", "Austin, Texas", "Jacksonville, Florida",
//                    "Fort Worth, Texas", "Columbus, Ohio", "Charlotte, North Carolina",
//                    "Indianapolis, Indiana", "San Francisco, California", "Seattle, Washington",
//                    "Denver, Colorado", "Washington, D.C.", "Boston, Massachusetts",
//                    "El Paso, Texas", "Nashville, Tennessee", "Detroit, Michigan",
//                    "Oklahoma City, Oklahoma", "Portland, Oregon", "Las Vegas, Nevada",
//                    "Memphis, Tennessee", "Louisville, Kentucky", "Baltimore, Maryland",
//                    "Milwaukee, Wisconsin", "Albuquerque, New Mexico", "Tucson, Arizona",
//                    "Fresno, California", "Sacramento, California", "Kansas City, Missouri",
//                    "Mesa, Arizona", "Atlanta, Georgia", "Long Beach, California",
//                    "Colorado Springs, Colorado", "Raleigh, North Carolina", "Miami, Florida",
//                    "Virginia Beach, Virginia", "Omaha, Nebraska", "Oakland, California",
//                    "Minneapolis, Minnesota", "Tulsa, Oklahoma", "Arlington, Texas",
//                    "New Orleans, Louisiana", "Wichita, Kansas"
//            };
//            List<String> houstonCities = new ArrayList<>(Collections.nCopies(8, "Houston, Texas"));
//            List<String> otherCities = new ArrayList<>();
//            for (String city : usCities) {
//                if (!city.equals("Houston, Texas")) {
//                    otherCities.add(city);
//                }
//            }
//
//            // Add 8 Houston hotels
//            for (int i = 0; i < 8; i++) {
//                hotels.add(createHotel(
//                        "Houston " + commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)],
//                        "Houston", "Texas", allAmenities, random
//                ));
//            }
//
//            // Add remaining hotels in other cities
//            for (int i = 0; i < 42; i++) { // 50 total - 8 Houston = 42
//                String[] cityState = otherCities.get(random.nextInt(otherCities.size())).split(", ");
//                hotels.add(createHotel(
//                        commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)] + " " + cityState[0],
//                        cityState[0], cityState[1], allAmenities, random
//                ));
//            }
//
//            
//            for (Hotel hotel : hotels) {
//                hotelRepository.save(hotel);
//                // --- 4. Create and Save Hotel Rooms for each hotel ---
//                // Each hotel will have a mix of room types and quantities...
//                int totalRoomsForHotel = 0;
//                // Initialize the hotelRooms collection if it's null (good practice, though usually done in Hotel constructor)
//                if (hotel.getHotelRoomTypes() == null) {
//                    hotel.setHotelRoomTypes(new HashSet<>());
//                }
//                if (hotel.getRooms() == null) { // Initialize physical rooms
//                    hotel.setRooms(new HashSet<>());
//                }
//
//                for (RoomType rt : allRoomTypes) {
//                    int numberOfRooms = 5 + random.nextInt(10); // 5 to 14 rooms of each type
//                    float basePrice = 50.0f + (rt.getTypeId() * 75.0f) + random.nextFloat() * 50.0f; // Varies by room type
//                    float discount = random.nextBoolean() ? random.nextFloat() * 0.1f : 0.0f; // 0% to 10% discount
//                    HotelRoom hotelRoom = createHotelRoom(hotel, rt, numberOfRooms, basePrice, discount, allAmenities, random);
//                    
//                    // IMPORTANT: Add the created room to the hotel's collection
//                    hotel.getHotelRoomTypes().add(hotelRoom);
//                    hotelRoomRepository.save(hotelRoom); // Save the room (will also persist its hotel relationship)
//
//                    // Create physical Room instances for each HotelRoom type
//                    for (int i = 0; i < numberOfRooms; i++) {
//                        Room physicalRoom = new Room();
//                        physicalRoom.setRoomNumber(rt.getName().substring(0,1).toUpperCase() + (100 + i)); // e.g., "S101", "D102"
//                        physicalRoom.setHotel(hotel);
//                        physicalRoom.setHotelRoomType(hotelRoom);
//                        physicalRoom.setAvailabilityStatus(Room.RoomAvailabilityStatus.AVAILABLE); // Corrected line
//                        roomRepository.save(physicalRoom);
//                        hotel.getRooms().add(physicalRoom); // Add to hotel's physical rooms
//                    }
//                    totalRoomsForHotel += numberOfRooms;
//                }
//                hotelRepository.save(hotel); // Re-save the hotel to potentially persist the collection changes
//                System.out.println("Created " + totalRoomsForHotel + " rooms for " + hotel.getHotelName());
//            }
//            System.out.println("Hotel and Room data populated for " + hotels.size() + " hotels.");
//
//            // --- Fetch users for relationships ---
//            Optional<User> user1Opt = userRepository.findByUsername("user");
//            Optional<User> adminOpt = userRepository.findByUsername("admin");
//            Optional<User> testUser1Opt = userRepository.findByUsername("testuser1");
//            Optional<User> testUser2Opt = userRepository.findByUsername("testuser2");
//            Optional<User> testUser3Opt = userRepository.findByUsername("testuser3");
//            Optional<User> testUser4Opt = userRepository.findByUsername("testuser4");
//            Optional<User> testUser5Opt = userRepository.findByUsername("testuser5");
//            User user1 = user1Opt.orElse(null);
//            User admin = adminOpt.orElse(null);
//            User testUser1 = testUser1Opt.orElse(null);
//            User testUser2 = testUser2Opt.orElse(null);
//            User testUser3 = testUser3Opt.orElse(null);
//            User testUser4 = testUser4Opt.orElse(null);
//            User testUser5 = testUser5Opt.orElse(null);
//            
//            System.out.println("user1: " + user1.getUsername());
//            System.out.println("testUser1: " + testUser1.getUsername());
//
//            // --- 5. Create and Save Loyalty Programs ---
//            System.out.println("Populating Loyalty Programs...");
//            LoyaltyProgram bronze = new LoyaltyProgram("BRONZE", "Entry-level tier with basic benefits.", 0, 0.02);
//            LoyaltyProgram silver = new LoyaltyProgram("SILVER", "Mid-tier with enhanced benefits.", 500, 0.05);
//            LoyaltyProgram gold = new LoyaltyProgram("GOLD", "Premium tier with exclusive benefits.", 1500, 0.10);
//            loyaltyProgramRepository.saveAll(Arrays.asList(bronze, silver, gold));
//            System.out.println("Loyalty Programs populated.");
////
////            // --- 6. Create and Save User Loyalty ---
////            System.out.println("Populating User Loyalty data...");
////            if (user1 != null) {
////                UserLoyalty ul1 = new UserLoyalty(user1, bronze, 100);
////                userLoyaltyRepository.save(ul1);
////            }
////            if (testUser1 != null) {
////                UserLoyalty ul2 = new UserLoyalty(testUser1, silver, 700);
////                userLoyaltyRepository.save(ul2);
////            }
////            System.out.println("User Loyalty data populated.");
//
//            // --- 7. Create and Save User Preferences ---
//            System.out.println("Populating User Preferences...");            
//            if (user1 != null) {
//                UserPreference up1 = new UserPreference(user1);
//                up1.setPreferredRoomType("Suite");
//                up1.setDietaryRestrictions("Vegetarian");
//                up1.setPreferredAmenities("Swimming Pool, Fitness Center");
//                up1.setCommunicationPreference("EMAIL");
//                userPreferenceRepository.save(up1);
//            }
//            if (testUser2 != null) {
//                UserPreference up2 = new UserPreference(testUser2);
//                up2.setPreferredRoomType("Deluxe");
//                up2.setCommunicationPreference("NONE");
//                userPreferenceRepository.save(up2);
//            }
//            System.out.println("User Preferences populated.");
//
//            // --- 8. Create and Save Bookings and Stays ---
//            System.out.println("Populating Bookings and Stays...");
//            Hotel randomHotel = hotels.get(random.nextInt(hotels.size()));
//            List<HotelRoom> hotelRoomsForRandomHotel = hotelRoomRepository.findByHotel(randomHotel);
//
//            if (user1 != null && !hotelRoomsForRandomHotel.isEmpty()) {
//                HotelRoom bookedHotelRoom1 = hotelRoomsForRandomHotel.get(random.nextInt(hotelRoomsForRandomHotel.size()));
//                Booking booking1 = new Booking(
//                    null, user1, bookedHotelRoom1, LocalDate.now().plusDays(random.nextInt(30)),
//                    LocalDate.now().plusDays(random.nextInt(30) + 1), 1, 2,
//                    bookedHotelRoom1.getPrice() * 1, Booking.BookingStatus.CONFIRMED, null, new HashSet<>()
//                );
//                booking1 = bookingRepository.save(booking1);
//
//                // Create a Stay for booking1
//                Optional<Room> availableRoom1 = roomRepository.findByHotelRoomTypeAndAvailabilityStatus(bookedHotelRoom1, Room.RoomAvailabilityStatus.OCCUPIED).stream().findFirst();
//                if (availableRoom1.isPresent()) {
//                    Room roomForStay1 = availableRoom1.get();
//                    roomForStay1.setAvailabilityStatus(Room.RoomAvailabilityStatus.OCCUPIED); // Corrected line
//                    roomRepository.save(roomForStay1);
//
//                    
//                    Stay stay1 = new Stay(
//                    	    null, // stayId (auto-generated, so null is fine)
//                    	    booking1, // booking
//                    	    user1, // user (corrected position)
//                    	    roomForStay1, // room (corrected position)
//                    	    booking1.getCheckInDate().atStartOfDay(), // Convert LocalDate to LocalDateTime
//                    	    booking1.getCheckOutDate().atStartOfDay(), // Convert LocalDate to LocalDateTime
//                    	    booking1.getTotalPrice(), // Use getTotalPrice() instead of getTotalAmount()
//                    	    Stay.StayStatus.IN_PROGRESS, // Set an appropriate initial status
//                    	    0 // loyaltyPointsEarned (default to 0, or calculate as needed)
//                    	);
//                    
//                    stay1 = stayRepository.save(stay1);
//                    booking1.getStays().add(stay1);
//                }
//                bookingRepository.save(booking1); // Re-save to persist stays relationship
//            }
//            System.out.println("Bookings and Stays populated.");
//
//            // --- 9. Create and Save Service Requests ---
////            System.out.println("Populating Service Requests...");
////            if (user1 != null) {
////                ServiceRequest sr1 = new ServiceRequest(
////                    user1, ServiceRequest.RequestType.HOUSEKEEPING, "Please clean the room thoroughly.",
////                    LocalDateTime.now(), ServiceRequest.RequestStatus.PENDING, "Room 101"
////                );
////                serviceRequestRepository.save(sr1);
////            }
////            if (testUser3 != null) {
////                ServiceRequest sr2 = new ServiceRequest(
////                    testUser3, ServiceRequest.RequestType.MAINTENANCE, "AC is not working in room 205.",
////                    LocalDateTime.now().minusDays(1), ServiceRequest.RequestStatus.IN_PROGRESS, "Room 205"
////                );
////                serviceRequestRepository.save(sr2);
////            }
////            System.out.println("Service Requests populated.");
////
////            // --- 10. Create and Save Facility Reservations ---
////            System.out.println("Populating Facility Reservations...");
////            if (user1 != null) {
////                FacilityReservation fr1 = new FacilityReservation(
////                    user1, FacilityReservation.FacilityType.GYM, LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Morning workout"
////                );
////                facilityReservationRepository.save(fr1);
////            }
////            if (testUser4 != null) {
////                FacilityReservation fr2 = new FacilityReservation(
////                    testUser4, FacilityReservation.FacilityType.SPA, LocalDate.now().plusDays(2), LocalTime.of(14, 30), "Full body massage"
////                );
////                facilityReservationRepository.save(fr2);
////            }
////            System.out.println("Facility Reservations populated.");
////
////            // --- 11. Create and Save Feedback ---
////            System.out.println("Populating Feedback...");
////            if (user1 != null) {
////                Feedback f1 = new Feedback(
////                    null, user1, hotels.get(0), null, Feedback.FeedbackType.PRAISE,
////                    "Excellent service and friendly staff!", LocalDateTime.now(), Feedback.FeedbackStatus.NEW, null
////                );
////                feedbackRepository.save(f1);
////            }
////            if (testUser5 != null) {
////                Feedback f2 = new Feedback(
////                    null, testUser5, hotels.get(1), null, Feedback.FeedbackType.COMPLAINT,
////                    "The Wi-Fi was very slow.", LocalDateTime.now(), Feedback.FeedbackStatus.NEW, null
////                );
////                feedbackRepository.save(f2);
////            }
////            System.out.println("Feedback populated.");
////
////            // --- 12. Create and Save Offers ---
//////            System.out.println("Populating Offers...");
//////            Offer offer1 = new Offer(
//////                null, "SUMMER20", "20% off all bookings for summer.", 0.20,
//////                LocalDate.now(), LocalDate.now().plusMonths(3)
//////            );
//////            offerRepository.save(offer1);
//////            Offer offer2 = new Offer(
//////                null, "WEEKEND10", "10% off weekend stays.", 0.10,
//////                LocalDate.now().minusMonths(1), LocalDate.now().plusDays(15)
//////            );
//////            offerRepository.save(offer2);
//////            System.out.println("Offers populated.");
////
//            System.out.println("Data initialization complete.");
//
//            // Example of how to add documents to vector store
//            List<Document> documents = new ArrayList<>();
//            documents.add(new Document("Hotel Amenities: " + allAmenities.stream().map(Amenities::getName).collect(Collectors.joining(", "))));
//            documents.add(new Document("Room Types: " + allRoomTypes.stream().map(RoomType::getName).collect(Collectors.joining(", "))));
//            documents.add(new Document("Popular Hotels: " + hotels.stream().limit(5).map(Hotel::getHotelName).collect(Collectors.joining(", "))));
//            
//            // Add amenity descriptions to the vector store
//            for (Amenities amenity : allAmenities) {
//                documents.add(new Document("Amenity: " + amenity.getName() + ". Description: " + getAmenityDescription(amenity.getName())));
//            }
//            vectorStore.add(documents);
//            System.out.println("Vector store populated with initial data.");
//        };
//    }
//
//    // Helper method to create hotels (as in the original file)
//    private Hotel createHotel(String name, String city, String state, List<Amenities> allAmenities, Random random) {
//        Hotel hotel = new Hotel();
//        hotel.setHotelName(name);
//        hotel.setAddress(random.nextInt(1000) + " " + (new String[]{"Park", "Main", "Oak", "Pine"}[random.nextInt(4)]) + " St");
//        hotel.setCity(city);
//        hotel.setState(state);
//        hotel.setStarRating(3 + random.nextInt(3)); // 3-5 stars
//        hotel.setAveragePrice(100.0 + random.nextFloat() * 200.0); // $100-$300
//        hotel.setDiscount(random.nextBoolean() ? random.nextFloat() * 0.15 : 0.0); // 0-15% discount
//        hotel.setDescription("A lovely hotel in " + city + " offering a comfortable stay.");
//        hotel.setEmail("info@" + name.toLowerCase().replaceAll(" ", "") + ".com");
//        hotel.setMobile("555-" + String.format("%04d", random.nextInt(10000)) + "-" + String.format("%04d", random.nextInt(10000)));
//        hotel.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=" + name.replaceAll(" ", "+"));
//        hotel.setTimesBooked(random.nextInt(500));
//        
//        // Assign a random subset of amenities
//        Set<Amenities> hotelAmenities = new HashSet<>();
//        int numAmenities = random.nextInt(allAmenities.size() / 2) + 1; // 1 to half of all amenities
//        for (int i = 0; i < numAmenities; i++) {
//            hotelAmenities.add(allAmenities.get(random.nextInt(allAmenities.size())));
//        }
//        hotel.setAmenities(hotelAmenities);
//        return hotel;
//    }
//
//    // Helper method to create hotel rooms (as in the original file)
//    private HotelRoom createHotelRoom(Hotel hotel, RoomType type, int totalRoomsOfType, float price, float discount, List<Amenities> allAmenities, Random random) {
//        HotelRoom hotelRoom = new HotelRoom();
//        hotelRoom.setHotel(hotel);
//        hotelRoom.setType(type);
//        hotelRoom.setTotalRoomsOfType(totalRoomsOfType);
//        hotelRoom.setPrice(price);
//        hotelRoom.setDiscount(discount);
//        hotelRoom.setDescription("A " + type.getName().toLowerCase() + " room at " + hotel.getHotelName());
//        hotelRoom.setPolicies("Standard cancellation policy applies.");
//
//        Set<Amenities> roomAmenities = new HashSet<>();
//        int numRoomAmenities = random.nextInt(allAmenities.size() / 3) + 1; // 1 to a third of all amenities
//        for (int i = 0; i < numRoomAmenities; i++) {
//            roomAmenities.add(allAmenities.get(random.nextInt(allAmenities.size())));
//        }
//        hotelRoom.setAmenities(roomAmenities);
//        
//        if (hotelRoom.getPhysicalRooms() == null) {
//            hotelRoom.setPhysicalRooms(new HashSet<>());
//        }
//
//        return hotelRoom;
//    }
//
//    // Helper method for amenity descriptions (as in the original file)
//    private String getAmenityDescription(String amenityName) {
//        switch (amenityName) {
//            case "Parking":
//                return "on-site parking available, often with valet service or self-parking options";
//            case "Free Wi-Fi":
//                return "complimentary internet access throughout the hotel premises";
//            case "Swimming Pool":
//                return "an on-site pool, which could be indoor, outdoor, or both, available for guest use";
//            case "Complimentary Breakfast":
//                return "breakfast provided by the hotel, included in the room rate";
//            case "Fitness Center":
//                return "a gym or health club with exercise equipment, weights, and sometimes personal trainers";
//            case "Bar & Lounge":
//                return "an on-site bar serving drinks and light snacks, often with a relaxed atmosphere";
//            case "Spa Services":
//                return "massages, facials, and other wellness treatments available for booking";
//            case "Pets Allowed":
//                return "pet-friendly rooms and amenities, though fees or restrictions may apply";
//            case "24-Hour Front Desk":
//                return "round-the-clock assistance for check-in, check-out, and guest inquiries";
//            case "Restaurant":
//                return "dining options available on-site, serving lunch and dinner";
//            case "Concierge Service":
//                return "personalized assistance for reservations, tours, and local recommendations";
//            case "Business Center":
//                return "facilities for printing, scanning, and internet access for business needs";
//            case "Laundry Service":
//                return "on-site laundry facilities or professional laundry and dry cleaning services";
//            case "Room Service":
//                return "in-room dining options available during specific hours";
//            case "Family Rooms":
//                return "larger rooms or connecting rooms suitable for families, often with extra beds or amenities for children";
//            default:
//                return "general services and facilities";
//        }
//    }
}










//package com.hotel.chatbox;
//
//import com.hotel.chatbox.domain.*;
//import com.hotel.chatbox.repository.*;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Random;
//import java.util.stream.Collectors;
//
//@SpringBootApplication
//public class HotelChatBoxApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(HotelChatBoxApplication.class, args);
//    }
//
//    /**
//     * CommandLineRunner to populate initial user data for testing.
//     * Creates a default 'user', 'admin', and 20 additional generic users.
//     */
//    
//    @Bean
//    @Order(1)
//    public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            // Create default 'user' if not exists
//            if (userRepository.findByUsername("user").isEmpty()) {
//                User user = new User();
//                user.setUsername("user");
//                user.setPassword(passwordEncoder.encode("password")); // Encode the password!
//                user.setRole("USER");
//                userRepository.save(user);
//                System.out.println("Created user: user/password");
//            }
//
//            // Create default 'admin' if not exists
//            if (userRepository.findByUsername("admin").isEmpty()) {
//                User admin = new User();
//                admin.setUsername("admin");
//                admin.setPassword(passwordEncoder.encode("adminpass")); // Encode the password!
//                admin.setRole("ADMIN");
//                userRepository.save(admin);
//                System.out.println("Created admin: admin/adminpass");
//            }
//
//            // Add 20 additional generic users
//            for (int i = 1; i <= 20; i++) {
//                String username = "testuser" + i;
//                if (userRepository.findByUsername(username).isEmpty()) {
//                    User testUser = new User();
//                    testUser.setUsername(username);
//                    testUser.setPassword(passwordEncoder.encode("password" + i)); // Unique password for each
//                    testUser.setRole("USER");
//                    userRepository.save(testUser);
//                    System.out.println("Created user: " + username + "/password" + i);
//                }
//            }
//            System.out.println("Finished populating initial user data.");
//        };
//    }
//
//    /**
//     * CommandLineRunner to initialize data in the database and vector store.
//     * This will run once when the application starts.
//     */
//    @Bean
//    @Order(2)
//    public CommandLineRunner dataInitializer(
//            HotelRepository hotelRepository,
//            RoomTypeRepository roomTypeRepository,
//            AmenitiesRepository amenitiesRepository,
//            HotelRoomRepository hotelRoomRepository,
//            UserRepository userRepository,
//            LoyaltyProgramRepository loyaltyProgramRepository,
//            UserLoyaltyRepository userLoyaltyRepository,
//            UserPreferenceRepository userPreferenceRepository,
//            BookingRepository bookingRepository,
//            FeedbackRepository feedbackRepository,
//            ServiceRequestRepository serviceRequestRepository,
//            FacilityReservationRepository facilityReservationRepository,
//            OfferRepository offerRepository,
//            VectorStore vectorStore) {
//        return args -> {
//            System.out.println("Initializing data and vector store...");
//            Random random = new Random();
//
//            // --- 1. Create and Save Amenities ---
//            List<Amenities> allAmenities = new ArrayList<>();
//            Amenities parking = new Amenities();
//            parking.setA_id(1);
//            parking.setName("Parking");
//            parking = amenitiesRepository.save(parking);
//            allAmenities.add(parking);
//
//            Amenities wifi = new Amenities();
//            wifi.setA_id(2);
//            wifi.setName("Free Wi-Fi");
//            wifi = amenitiesRepository.save(wifi);
//            allAmenities.add(wifi);
//
//            Amenities pool = new Amenities();
//            pool.setA_id(3);
//            pool.setName("Swimming Pool");
//            pool = amenitiesRepository.save(pool);
//            allAmenities.add(pool);
//            Amenities breakfast = new Amenities();
//            breakfast.setA_id(4);
//            breakfast.setName("Complimentary Breakfast");
//            breakfast = amenitiesRepository.save(breakfast);
//            allAmenities.add(breakfast);
//
//            Amenities fitness = new Amenities();
//            fitness.setA_id(5);
//            fitness.setName("Fitness Center");
//            fitness = amenitiesRepository.save(fitness);
//            allAmenities.add(fitness);
//
//            Amenities bar = new Amenities();
//            bar.setA_id(6);
//            bar.setName("Bar & Lounge");
//            bar = amenitiesRepository.save(bar);
//            allAmenities.add(bar);
//            Amenities spa = new Amenities();
//            spa.setA_id(7);
//            spa.setName("Spa Services");
//            spa = amenitiesRepository.save(spa);
//            allAmenities.add(spa);
//
//            Amenities petsAllowed = new Amenities();
//            petsAllowed.setA_id(8);
//            petsAllowed.setName("Pets Allowed");
//            petsAllowed = amenitiesRepository.save(petsAllowed);
//            allAmenities.add(petsAllowed);
//            
//            Amenities frontDesk24Hour = new Amenities();
//            frontDesk24Hour.setA_id(9);
//            frontDesk24Hour.setName("24-Hour Front Desk");
//            frontDesk24Hour = amenitiesRepository.save(frontDesk24Hour);
//            allAmenities.add(frontDesk24Hour);
//            Amenities restaurant = new Amenities();
//            restaurant.setA_id(10);
//            restaurant.setName("Restaurant");
//            restaurant = amenitiesRepository.save(restaurant);
//            allAmenities.add(restaurant);
//            
//            Amenities conciergeService = new Amenities();
//            conciergeService.setA_id(11);
//            conciergeService.setName("Concierge Service");
//            conciergeService = amenitiesRepository.save(conciergeService);
//            allAmenities.add(conciergeService);
//            
//            Amenities businessCenter = new Amenities();
//            businessCenter.setA_id(12);
//            businessCenter.setName("Business Center");
//            businessCenter = amenitiesRepository.save(businessCenter);
//            allAmenities.add(businessCenter);
//            Amenities laundryService = new Amenities();
//            laundryService.setA_id(13);
//            laundryService.setName("Laundry Service");
//            laundryService = amenitiesRepository.save(laundryService);
//            allAmenities.add(laundryService);
//            
//            Amenities roomService = new Amenities();
//            roomService.setA_id(14);
//            roomService.setName("Room Service");
//            roomService = amenitiesRepository.save(roomService);
//            allAmenities.add(roomService);
//            
//            Amenities familyRooms = new Amenities();
//            familyRooms.setA_id(15);
//            familyRooms.setName("Family Rooms");
//            familyRooms = amenitiesRepository.save(familyRooms);
//            allAmenities.add(familyRooms);
//            
//            System.out.println("Amenities data populated.");
//            // --- 2. Create and Save Room Types ---  
//            List<RoomType> allRoomTypes = new ArrayList<>();
//            RoomType standard = new RoomType();
//            standard.setTypeId(1);
//            standard.setName("Standard");
//            standard = roomTypeRepository.save(standard);
//            allRoomTypes.add(standard);
//
//            RoomType deluxe = new RoomType();
//            deluxe.setTypeId(2);
//            deluxe.setName("Deluxe");
//            deluxe = roomTypeRepository.save(deluxe);
//            allRoomTypes.add(deluxe);
//
//            RoomType suite = new RoomType();
//            suite.setTypeId(3);
//            suite.setName("Suite");
//            suite = roomTypeRepository.save(suite);
//            allRoomTypes.add(suite);
//            
//            System.out.println("Room Types data populated.");
//            // --- 3. Create and Save Hotels ---
//            Hotel hotel1 = new Hotel();
//            hotel1.setHotelName("Grand Plaza Hotel");
//            hotel1.setAddress("123 Main St");
//            hotel1.setCity("New York City");
//            hotel1.setState("New York");
//            hotel1.setStarRating(5);
//            hotel1.setAveragePrice(250.00);
//            hotel1.setDiscount(0.10);
//            hotel1.setDescription("Experience luxury and comfort at our downtown hotel with stunning city views.");
//            hotel1.setEmail("info@grandplaza.com");
//            hotel1.setMobile("123-456-7890");
//            hotel1.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Grand+Plaza");
//            hotel1.setTimesBooked(150);
//            hotel1.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar)));
//            hotel1 = hotelRepository.save(hotel1);
//            Set<Amenities> hotelAmenityNames = new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar));
//            Hotel hotel2 = new Hotel();
//            hotel2.setHotelName("Riverside Inn");
//            hotel2.setAddress("45 River Rd");
//            hotel2.setCity("Austin");
//            hotel2.setState("Texas");
//            hotel2.setStarRating(3);
//            hotel2.setAveragePrice(120.00);
//            hotel2.setDiscount(0.05);
//            hotel2.setDescription("A cozy inn by the river, perfect for a relaxing getaway. Pet-friendly options available.");
//            hotel2.setEmail("contact@riversideinn.com");
//            hotel2.setMobile("987-654-3210");
//            hotel2.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Riverside+Inn");
//            hotel2.setTimesBooked(80);
//            hotel2.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, petsAllowed)));
//            hotel2 = hotelRepository.save(hotel2);
//            
//
//            // --- 3. Create and Save 50 Hotels ---
//            List<Hotel> hotels = new ArrayList<>();
//            String[] commonHotelNames = {"Grand", "Majestic", "Royal", "Downtown", "Riverside", "City Center", "Inn", "Plaza", "Heights", "Bayview"};
//            String[] adjectives = {"Comfort", "Luxury", "Elite", "Prime", "Cornerstone", "Metropolitan", "Harmony", "Azure", "Summit", "Evergreen"};
//            String[] locations = {"Hotel", "Suites", "Lodge", "Resort", "Motel", "Boutique", "Palace"};
//            String[] usCities = {
//                    "New York City, New York", "Los Angeles, California", "Chicago, Illinois",
//                    "Houston, Texas", "Phoenix, Arizona", "Philadelphia, Pennsylvania",
//                    "San Antonio, Texas", "San Diego, California", "Dallas, Texas",
//                    "San Jose, California", "Austin, Texas", "Jacksonville, Florida",
//                    "Fort Worth, Texas", "Columbus, Ohio", "Charlotte, North Carolina",
//                    "Indianapolis, Indiana", "San Francisco, California", "Seattle, Washington",
//                    "Denver, Colorado", "Washington, D.C.", "Boston, Massachusetts",
//                    "El Paso, Texas", "Nashville, Tennessee", "Detroit, Michigan",
//                    "Oklahoma City, Oklahoma", "Portland, Oregon", "Las Vegas, Nevada",
//                    "Memphis, Tennessee", "Louisville, Kentucky", "Baltimore, Maryland",
//                    "Milwaukee, Wisconsin", "Albuquerque, New Mexico", "Tucson, Arizona",
//                    "Fresno, California", "Sacramento, California", "Kansas City, Missouri",
//                    "Mesa, Arizona", "Atlanta, Georgia", "Long Beach, California",
//                    "Colorado Springs, Colorado", "Raleigh, North Carolina", "Miami, Florida",
//                    "Virginia Beach, Virginia", "Omaha, Nebraska", "Oakland, California",
//                    "Minneapolis, Minnesota", "Tulsa, Oklahoma", "Arlington, Texas",
//                    "New Orleans, Louisiana", "Wichita, Kansas"
//            };
//            List<String> houstonCities = new ArrayList<>(Collections.nCopies(8, "Houston, Texas"));
//            List<String> otherCities = new ArrayList<>();
//            for (String city : usCities) {
//                if (!city.equals("Houston, Texas")) {
//                    otherCities.add(city);
//                }
//            }
//
//            // Add 8 Houston hotels
//            for (int i = 0; i < 8; i++) {
//                hotels.add(createHotel(
//                        "Houston " + commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)],
//                        "Houston", "Texas", allAmenities, random
//                ));
//            }
//
//            // Add remaining hotels in other cities
//            for (int i = 0; i < 42; i++) { // 50 total - 8 Houston = 42
//                String[] cityState = otherCities.get(random.nextInt(otherCities.size())).split(", ");
//                hotels.add(createHotel(
//                        commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)] + " " + cityState[0],
//                        cityState[0], cityState[1], allAmenities, random
//                ));
//            }
//
//            
//            for (Hotel hotel : hotels) {
//                hotelRepository.save(hotel);
//                // --- 4. Create and Save Hotel Rooms for each hotel ---
//                // Each hotel will have a mix of room types and quantities
//                int totalRoomsForHotel = 0;
//                
//                // Initialize the hotelRooms collection if it's null (good practice, though usually done in Hotel constructor)
//                if (hotel.getHotelRooms() == null) {
//                    hotel.setHotelRooms(new HashSet<>());
//                }
//
//                for (RoomType rt : allRoomTypes) {
//                    int numberOfRooms = 5 + random.nextInt(10); // 5 to 14 rooms of each type
//                    float basePrice = 50.0f + (rt.getTypeId() * 75.0f) + random.nextFloat() * 50.0f; // Varies by room type
//                    float discount = random.nextBoolean() ?
//                            random.nextFloat() * 0.1f : 0.0f; // 0% to 10% discount
//
//                    HotelRoom hotelRoom = createHotelRoom(hotel, rt, numberOfRooms, basePrice, discount, allAmenities, random);
//                    
//                    // IMPORTANT: Add the created room to the hotel's collection
//                    hotel.getHotelRooms().add(hotelRoom); 
//                    
//                    hotelRoomRepository.save(hotelRoom); // Save the room (will also persist its hotel relationship)
//                    totalRoomsForHotel += numberOfRooms;
//                }
//                hotelRepository.save(hotel); // Re-save the hotel to potentially persist the collection changes
//                System.out.println("Created " + totalRoomsForHotel + " rooms for " + hotel.getHotelName());
//            }
//            
//            System.out.println("Hotel and Room data populated for " + hotels.size() + " hotels.");
//            
//            // --- Fetch users for relationships ---
//            Optional<User> user1Opt = userRepository.findByUsername("user");
//            Optional<User> adminOpt = userRepository.findByUsername("admin");
//            Optional<User> testUser1Opt = userRepository.findByUsername("testuser1");
//            Optional<User> testUser2Opt = userRepository.findByUsername("testuser2");
//            Optional<User> testUser3Opt = userRepository.findByUsername("testuser3");
//            Optional<User> testUser4Opt = userRepository.findByUsername("testuser4");
//            Optional<User> testUser5Opt = userRepository.findByUsername("testuser5");
//
//            User user1 = user1Opt.orElse(null);
//            User admin = adminOpt.orElse(null);
//            User testUser1 = testUser1Opt.orElse(null);
//            User testUser2 = testUser2Opt.orElse(null);
//            User testUser3 = testUser3Opt.orElse(null);
//            User testUser4 = testUser4Opt.orElse(null);
//            User testUser5 = testUser5Opt.orElse(null);
//            //
//////            // --- 5. Create and Save Loyalty Programs ---
//////            LoyaltyProgram bronze = new LoyaltyProgram("BRONZE", "Entry-level tier", 0, 0.0);
//////            LoyaltyProgram silver = new LoyaltyProgram("SILVER", "Achieved after 500 points, 5% discount.", 500, 0.05);
//////            LoyaltyProgram gold = new LoyaltyProgram("GOLD", "Achieved after 1500 points, 10% discount and late checkout.", 1500, 0.10);
//////
//////            bronze = loyaltyProgramRepository.save(bronze);
//////            silver = loyaltyProgramRepository.save(silver);
//////            gold = loyaltyProgramRepository.save(gold);
//////            System.out.println("Loyalty Programs populated.");
//////
//////            // --- 6. Create and Save User Loyalty Info ---
//////            if (user1 != null) {
//////                userLoyaltyRepository.save(new UserLoyalty(user1, 100, bronze));
//////            }
//////            if (testUser1 != null) {
//////                userLoyaltyRepository.save(new UserLoyalty(testUser1, 600, silver));
//////            }
//////            if (testUser2 != null) {
//////                userLoyaltyRepository.save(new UserLoyalty(testUser2, 1600, gold));
//////            }
//////            System.out.println("User Loyalty data populated.");
//////
//////            // --- 7. Create and Save User Preferences ---
//////            if (user1 != null) {
//////                UserPreference pref1 = new UserPreference(user1);
//////                pref1.setPreferredRoomType("DELUXE");
//////                pref1.setDietaryRestrictions("Vegetarian");
//////                pref1.setCommunicationPreference("EMAIL");
//////                userPreferenceRepository.save(pref1);
//////            }
//////            if (testUser3 != null) {
//////                UserPreference pref3 = new UserPreference(testUser3);
//////                pref3.setPreferredRoomType("SUITE");
//////                pref3.setPreferredAmenities("Swimming Pool, Spa Services");
//////                pref3.setCommunicationPreference("SMS");
//////                userPreferenceRepository.save(pref3);
//////            }
//////            System.out.println("User Preference data populated.");
////
////
//            // --- 8. Create and Save Bookings ---
//            LocalDate today = LocalDate.now();
//            
////            Hotel h = hotels.get(1);
////            
////            System.out.println("Hotel:  " + h.getHotelName());
////            
////            Set<HotelRoom> hr = hotels.get(3).getHotelRooms();
////            
////            System.out.println("Rooms:  ");
////            hr.forEach(s -> System.out.println(s.toString()));
//            
//            
//            if (user1 != null) {
//                // Past booking
//                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.minusDays(10), today.minusDays(7), 1, 2, 360.00, Booking.BookingStatus.COMPLETED));
//                // Current booking (checking availability)
//                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.plusDays(1), today.plusDays(3), 2, 4, 1080.00, Booking.BookingStatus.CONFIRMED));
//                // 2 rooms booked
//                // Future booking
//                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.plusMonths(1), today.plusMonths(1).plusDays(5), 1, 2, 1500.00, Booking.BookingStatus.PENDING));
//            }
//            if (testUser1 != null) {
//                // Future booking in a different hotel
//                bookingRepository.save(new Booking(testUser1, hotels.get(1).getHotelRooms().iterator().next(), today.plusWeeks(2), today.plusWeeks(2).plusDays(3), 1, 2, 270.00, Booking.BookingStatus.CONFIRMED));
//            }
//            System.out.println("Booking data populated.");
//            
//            
////            
////            
////            // --- 9. Create and Save Feedback ---
////            if (user1 != null) {
////                feedbackRepository.save(new Feedback(user1, hotels.get(0), Feedback.FeedbackType.COMPLAINT, "No hot water in the shower this morning."));
////                feedbackRepository.save(new Feedback(user1, hotels.get(1), Feedback.FeedbackType.PRAISE, "The breakfast at Riverside Inn was excellent!"));
////            }
////            if (testUser2 != null) {
////                feedbackRepository.save(new Feedback(testUser2, hotels.get(0), Feedback.FeedbackType.SUGGESTION, "Could add more vegan options to room service menu."));
////            }
////            System.out.println("Feedback data populated.");
////            // --- 10. Create and Save Service Requests ---
////            if (user1 != null) {
////                serviceRequestRepository.save(new ServiceRequest(user1, hotels.get(0).getHotelRooms().iterator().next(), ServiceRequest.RequestType.ROOM_SERVICE, "Coffee and croissant to room 101 at 8 AM."));
////                serviceRequestRepository.save(new ServiceRequest(user1, hotels.get(0).getHotelRooms().iterator().next(), ServiceRequest.RequestType.HOUSEKEEPING, "Please clean the room thoroughly."));
////            }
////            if (testUser3 != null) {
////                serviceRequestRepository.save(new ServiceRequest(testUser3, hotels.get(1).getHotelRooms().iterator().next(), ServiceRequest.RequestType.MAINTENANCE, "Light bulb in bathroom is out."));
////            }
////            System.out.println("Service Request data populated.");
////            // --- 11. Create and Save Facility Reservations ---
////            if (user1 != null) {
////                facilityReservationRepository.save(new FacilityReservation(user1, hotels.get(0), FacilityReservation.FacilityType.SPA, today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
////            }
////            if (testUser4 != null) {
////                facilityReservationRepository.save(new FacilityReservation(testUser4, hotels.get(0), FacilityReservation.FacilityType.GYM, today, LocalTime.of(7, 30), LocalTime.of(8, 30)));
////            }
////            System.out.println("Facility Reservation data populated.");
//            
//            
//            
//            
//            
//            
////            // --- 12. Create and Save Offers ---
////            Offer summerSale = new Offer("SUMMER25", "Summer Holiday Sale", "25% off all bookings for July, perfect for your summer getaway!", Offer.DiscountType.PERCENTAGE, 0.25,
////                    LocalDate.of(2025, 6, 10), LocalDate.of(2025, 7, 31));
////            Offer weekendGetaway = new Offer("WEEKEND100", "Weekend Escape Deal", "Flat $100 off on weekend stays, relax and unwind!", Offer.DiscountType.FIXED_AMOUNT, 100.0,
////                    LocalDate.of(2025, 6, 15), LocalDate.of(2025, 8, 31));
////            Offer goldMemberDeal = new Offer("GOLDEXTRA", "Exclusive Gold Member Discount", "Additional 5% off for Gold members on any booking!", Offer.DiscountType.PERCENTAGE, 0.05,
////                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 9, 30));
////            goldMemberDeal.setTargetLoyaltyTier("GOLD");
////
////            Offer minBookingOffer = new Offer("LARGEBOOKING", "Big Booking Bonus", "15% off for bookings over $500, the more you spend, the more you save!", Offer.DiscountType.PERCENTAGE, 0.15,
////                    LocalDate.of(2025, 6, 12), LocalDate.of(2025, 7, 15));
////            minBookingOffer.setMinBookingAmount(500.0);
////
////            Offer repeatGuestOffer = new Offer("REPEAT20", "Welcome Back Discount", "20% off for our valued guests with 3 or more previous bookings!", Offer.DiscountType.PERCENTAGE, 0.20,
////                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 1));
////            repeatGuestOffer.setMinBookingsRequired(3);
////
////            // Add an expired offer to demonstrate filtering
////            Offer expiredOffer = new Offer("EXPIRED10", "Early Bird Special (Expired)", "10% off for early birds, this offer is no longer valid.", Offer.DiscountType.PERCENTAGE, 0.10,
////                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31));
////            expiredOffer.setActive(false);
////
////
////            summerSale = offerRepository.save(summerSale);
////            weekendGetaway = offerRepository.save(weekendGetaway);
////            goldMemberDeal = offerRepository.save(goldMemberDeal);
////            minBookingOffer = offerRepository.save(minBookingOffer);
////            repeatGuestOffer = offerRepository.save(repeatGuestOffer);
////            expiredOffer = offerRepository.save(expiredOffer);
////            System.out.println("Offer data populated.");
//            
//            // --- 13. Populate Vector Store with Documents ---
//            List<Document> documents = new ArrayList<>();
//            // Add hotel descriptions to documents
//            for (Hotel hotel : hotels) {
//                documents.add(Document.builder()
//                        .text("Hotel Name: " + hotel.getHotelName() + ". Description: " + hotel.getDescription() + ". Amenities: " +
//                                hotel.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) +
//                                ". Address: " + hotel.getAddress() + ", " + hotel.getCity() + ", " + hotel.getState() +
//                                ". Star Rating: " + hotel.getStarRating() + ".")
//                        .metadata(Map.of("type", "hotel_description", "source_id", hotel.getHotelId()))
//                        .build());
//                // Add hotel room descriptions to documents
//                for (HotelRoom room : hotel.getHotelRooms()) {
//                    // Determine a reasonable guest capacity based on room type
//                    String guestCapacity;
//                    switch (room.getType().getName()) {
//                        case "Standard":
//                            guestCapacity = "1-2 guests";
//                            break;
//                        case "Deluxe":
//                            guestCapacity = "2-3 guests";
//                            break;
//                        case "Suite":
//                            guestCapacity = "2-5 guests";
//                            break;
//                        default:
//                            guestCapacity = "unknown guests";
//                    }
//
//                    documents.add(Document.builder()
//                            .text("Room type: " + room.getType().getName() + ". Description: " + room.getDescription() +
//                                    ". Price per night: $" + String.format("%.2f", room.getPrice()) +
//                                    ". Number of rooms available: " + room.getNoRooms() +
//                                    ". Accommodates: " + guestCapacity +
//                                    ". Amenities: " + room.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) +
//                                    ". Policies: " + room.getPolicies() +
//                                    ". Associated Hotel: " + room.getHotel().getHotelName() +
//                                    ". Located at: " + room.getHotel().getAddress() + ", " + room.getHotel().getCity() + ", " + room.getHotel().getState() + ".")
//                            .metadata(Map.of("type", "room_description",
//                                    "source_id", room.getHotelRoomId(),
//                                    "hotel_id", room.getHotel().getHotelId(),
//                                    "room_type", room.getType().getName(),
//                                    "number_of_rooms_available", room.getNoRooms(),
//                                    "price", room.getPrice(),
//                                    "amenities", room.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")),
//                                    "city", room.getHotel().getCity(),
//                                    "state", room.getHotel().getState(),
//                                    "address", room.getHotel().getAddress()))
//                            .build());
//                }
//            }
//
//            // Add amenity descriptions to documents
//            for (Amenities amenity : allAmenities) {
//                documents.add(Document.builder()
//                        .text("Amenity: " + amenity.getName() + " is available and typically includes " + getAmenityDescription(amenity.getName()) + ".")
//                        .metadata(Map.of("type", "amenity_info", "source_id", amenity.getA_id()))
//                        .build());
//            }
//
//            // Log documents before adding to vector store
//            System.out.println("DEBUG: Documents prepared for vector store:");
//            documents.forEach(doc -> System.out.println("- " + doc.getText() + " (Metadata: " + doc.getMetadata() + ")"));
//            // Add documents to the vector store
//            vectorStore.add(documents);
//            System.out.println("Data initialization complete. Vector store populated with " + documents.size() + " documents.");
//            
//            System.out.println("documents");
//            documents.forEach(s -> System.out.println(s.toString()));
//          
//        };
//    }
//
//    // --- Helper Methods for Data Generation ---
//
//    private Hotel createHotel(String name, String city, String state, List<Amenities> allAmenities, Random random) {
//        Hotel hotel = new Hotel();
//        hotel.setHotelName(name);
//        hotel.setAddress(random.nextInt(999) + 1 + " " + (new String[]{"Oak", "Pine", "Maple", "Elm", "Cedar"})[random.nextInt(5)] + " St");
//        hotel.setCity(city);
//        hotel.setState(state);
//        hotel.setStarRating(3 + random.nextInt(3)); // 3 to 5 stars
//        hotel.setAveragePrice(100.00 + random.nextInt(200) + random.nextDouble()); // 100-300
//        hotel.setDiscount(random.nextBoolean() ? random.nextDouble() * 0.15 : 0.0); // 0-15% discount
//        hotel.setDescription("Discover " + name + ", a premier " + hotel.getStarRating() + "-star hotel in " + city + ", " + state + ". " +
//                "Enjoy modern amenities and exceptional service for a memorable stay.");
//        hotel.setEmail("info@" + name.toLowerCase().replaceAll(" ", "") + ".com");
//        hotel.setMobile(String.format("%03d-%03d-%04d", random.nextInt(1000), random.nextInt(1000), random.nextInt(10000)));
//        hotel.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=" + name.replaceAll(" ", "+"));
//        hotel.setTimesBooked(random.nextInt(500)); // Up to 500 bookings
//
//        // Assign a random subset of amenities
//        Set<Amenities> assignedAmenities = new HashSet<>();
//        int numAmenities = 3 + random.nextInt(5); // 3 to 7 amenities per hotel
//        for (int i = 0; i < numAmenities; i++) {
//            assignedAmenities.add(allAmenities.get(random.nextInt(allAmenities.size())));
//        }
//        hotel.setAmenities(assignedAmenities);
//        return hotel;
//    }
//
//    private HotelRoom createHotelRoom(Hotel hotel, RoomType roomType, int noRooms, float price, float discount, List<Amenities> allAmenities, Random random) {
//        HotelRoom room = new HotelRoom();
//        room.setHotel(hotel);
//        room.setType(roomType);
//        room.setNoRooms(noRooms);
//        room.setPrice(price);
//        room.setDiscount(discount);
//        room.setDescription(roomType.getName() + " room with " + (random.nextBoolean() ? "a king-size bed" : "two queen beds") + ". " +
//                (random.nextBoolean() ? "Enjoy city views." : "Features a spacious layout."));
//        room.setPolicies("No smoking. Check-in 3 PM, Check-out 11 AM. " + (random.nextBoolean() ? "Complimentary cancellation up to 24 hours prior." : "Flexible booking options."));
//        Set<Amenities> roomAmenities = new HashSet<>();
//        roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Free Wi-Fi")).findFirst().orElse(null)); // Always add Wi-Fi
//        if (random.nextBoolean()) {
//            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Complimentary Breakfast")).findFirst().orElse(null));
//        }
//        if (roomType.getName().equals("Suite") && random.nextBoolean()) {
//            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Room Service")).findFirst().orElse(null));
//        }
//        if (hotel.getAmenities().contains(allAmenities.stream().filter(a -> a.getName().equals("Pets Allowed")).findFirst().orElse(null)) && random.nextBoolean()) {
//            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Pets Allowed")).findFirst().orElse(null));
//        }
//        roomAmenities.remove(null); // Remove any nulls if amenity not found
//        room.setAmenities(roomAmenities);
//        return room;
//    }
//
//    private String getAmenityDescription(String amenityName) {
//        switch (amenityName) {
//            case "Parking":
//                return "on-site parking facilities, valet service, or nearby parking options";
//            case "Free Wi-Fi":
//                return "high-speed internet access throughout the property and in all rooms";
//            case "Swimming Pool":
//                return "indoor or outdoor pools, sometimes with hot tubs or poolside service";
//            case "Complimentary Breakfast":
//                return "a breakfast buffet or continental breakfast included with your stay";
//            case "Fitness Center":
//                return "gym access with cardio equipment, weights, and sometimes personal trainers";
//            case "Bar & Lounge":
//                return "an on-site bar serving drinks and light snacks, often with a relaxed atmosphere";
//            case "Spa Services":
//                return "massages, facials, and other wellness treatments available for booking";
//            case "Pets Allowed":
//                return "pet-friendly rooms and amenities, though fees or restrictions may apply";
//            case "24-Hour Front Desk":
//                return "round-the-clock assistance for check-in, check-out, and guest inquiries";
//            case "Restaurant":
//                return "dining options available on-site, serving lunch and dinner";
//            case "Concierge Service":
//                return "personalized assistance for reservations, tours, and local recommendations";
//            case "Business Center":
//                return "facilities for printing, scanning, and internet access for business needs";
//            case "Laundry Service":
//                return "on-site laundry facilities or professional laundry and dry cleaning services";
//            case "Room Service":
//                return "in-room dining options available during specific hours";
//            case "Family Rooms":
//                return "larger rooms or connecting rooms suitable for families, often with extra beds or amenities for children";
//            default:
//                return "general services and facilities";
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
////package com.hotel.chatbox;
////
////import com.hotel.chatbox.domain.*;
////import com.hotel.chatbox.repository.*;
////import org.springframework.boot.CommandLineRunner;
////import org.springframework.boot.SpringApplication;
////import org.springframework.boot.autoconfigure.SpringBootApplication;
////import org.springframework.context.annotation.Bean;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.ai.document.Document;
////import org.springframework.ai.vectorstore.VectorStore;
////
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.time.LocalTime;
////import java.util.ArrayList;
////import java.util.Arrays;
////import java.util.Collections;
////import java.util.HashSet;
////import java.util.List;
////import java.util.Set;
////import java.util.Map;
////import java.util.Optional;
////import java.util.Random;
////import java.util.stream.Collectors;
////
////@SpringBootApplication
////public class HotelChatBoxApplication {
////
////    public static void main(String[] args) {
////        SpringApplication.run(HotelChatBoxApplication.class, args);
////    }
////
////    /**
////     * CommandLineRunner to populate initial user data for testing.
////     * Creates a default 'user', 'admin', and 20 additional generic users.
////     */
////    @Bean
////    public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
////        return args -> {
////            // Create default 'user' if not exists
////            if (userRepository.findByUsername("user").isEmpty()) {
////                User user = new User();
////                user.setUsername("user");
////                user.setPassword(passwordEncoder.encode("password")); // Encode the password!
////                user.setRole("USER");
////                userRepository.save(user);
////                System.out.println("Created user: user/password");
////            }
////
////            // Create default 'admin' if not exists
////            if (userRepository.findByUsername("admin").isEmpty()) {
////                User admin = new User();
////                admin.setUsername("admin");
////                admin.setPassword(passwordEncoder.encode("adminpass")); // Encode the password!
////                admin.setRole("ADMIN");
////                userRepository.save(admin);
////                System.out.println("Created admin: admin/adminpass");
////            }
////
////            // Add 20 additional generic users
////            for (int i = 1; i <= 20; i++) {
////                String username = "testuser" + i;
////                if (userRepository.findByUsername(username).isEmpty()) {
////                    User testUser = new User();
////                    testUser.setUsername(username);
////                    testUser.setPassword(passwordEncoder.encode("password" + i)); // Unique password for each
////                    testUser.setRole("USER");
////                    userRepository.save(testUser);
////                    System.out.println("Created user: " + username + "/password" + i);
////                }
////            }
////            System.out.println("Finished populating initial user data.");
////        };
////    }
////
////    /**
////     * CommandLineRunner to initialize data in the database and vector store.
////     * This will run once when the application starts.
////     */
////    @Bean
////    public CommandLineRunner dataInitializer(
////            HotelRepository hotelRepository,
////            RoomTypeRepository roomTypeRepository,
////            AmenitiesRepository amenitiesRepository,
////            HotelRoomRepository hotelRoomRepository,
////            UserRepository userRepository,
////            LoyaltyProgramRepository loyaltyProgramRepository,
////            UserLoyaltyRepository userLoyaltyRepository,
////            UserPreferenceRepository userPreferenceRepository,
////            BookingRepository bookingRepository,
////            FeedbackRepository feedbackRepository,
////            ServiceRequestRepository serviceRequestRepository,
////            FacilityReservationRepository facilityReservationRepository,
////            OfferRepository offerRepository,
////            VectorStore vectorStore) {
////        return args -> {
////            System.out.println("Initializing data and vector store...");
////
////            Random random = new Random();
////
////            // --- 1. Create and Save Amenities ---
////            List<Amenities> allAmenities = new ArrayList<>();
////
////            Amenities parking = new Amenities();
////            parking.setA_id(1);
////            parking.setName("Parking");
////            parking = amenitiesRepository.save(parking);
////            allAmenities.add(parking);
////
////            Amenities wifi = new Amenities();
////            wifi.setA_id(2);
////            wifi.setName("Free Wi-Fi");
////            wifi = amenitiesRepository.save(wifi);
////            allAmenities.add(wifi);
////
////            Amenities pool = new Amenities();
////            pool.setA_id(3);
////            pool.setName("Swimming Pool");
////            pool = amenitiesRepository.save(pool);
////            allAmenities.add(pool);
////
////            Amenities breakfast = new Amenities();
////            breakfast.setA_id(4);
////            breakfast.setName("Complimentary Breakfast");
////            breakfast = amenitiesRepository.save(breakfast);
////            allAmenities.add(breakfast);
////
////            Amenities fitness = new Amenities();
////            fitness.setA_id(5);
////            fitness.setName("Fitness Center");
////            fitness = amenitiesRepository.save(fitness);
////            allAmenities.add(fitness);
////
////            Amenities bar = new Amenities();
////            bar.setA_id(6);
////            bar.setName("Bar & Lounge");
////            bar = amenitiesRepository.save(bar);
////            allAmenities.add(bar);
////
////            Amenities spa = new Amenities();
////            spa.setA_id(7);
////            spa.setName("Spa Services");
////            spa = amenitiesRepository.save(spa);
////            allAmenities.add(spa);
////
////            Amenities petsAllowed = new Amenities();
////            petsAllowed.setA_id(8);
////            petsAllowed.setName("Pets Allowed");
////            petsAllowed = amenitiesRepository.save(petsAllowed);
////            allAmenities.add(petsAllowed);
////            
////            Amenities frontDesk24Hour = new Amenities();
////            frontDesk24Hour.setA_id(9);
////            frontDesk24Hour.setName("24-Hour Front Desk");
////            frontDesk24Hour = amenitiesRepository.save(frontDesk24Hour);
////            allAmenities.add(frontDesk24Hour);
////            
////            Amenities restaurant = new Amenities();
////            restaurant.setA_id(10);
////            restaurant.setName("Restaurant");
////            restaurant = amenitiesRepository.save(restaurant);
////            allAmenities.add(restaurant);
////            
////            Amenities conciergeService = new Amenities();
////            conciergeService.setA_id(11);
////            conciergeService.setName("Concierge Service");
////            conciergeService = amenitiesRepository.save(conciergeService);
////            allAmenities.add(conciergeService);
////            
////            Amenities businessCenter = new Amenities();
////            businessCenter.setA_id(12);
////            businessCenter.setName("Business Center");
////            businessCenter = amenitiesRepository.save(businessCenter);
////            allAmenities.add(businessCenter);
////            
////            Amenities laundryService = new Amenities();
////            laundryService.setA_id(13);
////            laundryService.setName("Laundry Service");
////            laundryService = amenitiesRepository.save(laundryService);
////            allAmenities.add(laundryService);
////            
////            Amenities roomService = new Amenities();
////            roomService.setA_id(14);
////            roomService.setName("Room Service");
////            roomService = amenitiesRepository.save(roomService);
////            allAmenities.add(roomService);
////            
////            Amenities familyRooms = new Amenities();
////            familyRooms.setA_id(15);
////            familyRooms.setName("Family Rooms");
////            familyRooms = amenitiesRepository.save(familyRooms);
////            allAmenities.add(familyRooms);
////            
////            System.out.println("Amenities data populated.");
////            
////            
////
////            // --- 2. Create and Save Room Types ---  
////            List<RoomType> allRoomTypes = new ArrayList<>();
////            
////            RoomType standard = new RoomType();
////            standard.setTypeId(1);
////            standard.setName("Standard");
////            standard = roomTypeRepository.save(standard);
////            allRoomTypes.add(standard);
////
////            RoomType deluxe = new RoomType();
////            deluxe.setTypeId(2);
////            deluxe.setName("Deluxe");
////            deluxe = roomTypeRepository.save(deluxe);
////            allRoomTypes.add(deluxe);
////
////            RoomType suite = new RoomType();
////            suite.setTypeId(3);
////            suite.setName("Suite");
////            suite = roomTypeRepository.save(suite);
////            allRoomTypes.add(suite);
////            
////            System.out.println("Room Types data populated.");
////            
////            // --- 3. Create and Save Hotels ---
////            Hotel hotel1 = new Hotel();
////            hotel1.setHotelName("Grand Plaza Hotel");
////            hotel1.setAddress("123 Main St");
////            hotel1.setCity("New York City");
////            hotel1.setState("New York");
////            hotel1.setStarRating(5);
////            hotel1.setAveragePrice(250.00);
////            hotel1.setDiscount(0.10);
////            hotel1.setDescription("Experience luxury and comfort at our downtown hotel with stunning city views.");
////            hotel1.setEmail("info@grandplaza.com");
////            hotel1.setMobile("123-456-7890");
////            hotel1.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Grand+Plaza");
////            hotel1.setTimesBooked(150);
////            hotel1.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar)));
////            hotel1 = hotelRepository.save(hotel1);
////            
//////            List<String> z = 
////            Set<Amenities> hotelAmenityNames = new HashSet<>(Arrays.asList(parking, wifi, pool, breakfast, fitness, bar));
////
////
////            Hotel hotel2 = new Hotel();
////            hotel2.setHotelName("Riverside Inn");
////            hotel2.setAddress("45 River Rd");
////            hotel2.setCity("Austin");
////            hotel2.setState("Texas");
////            hotel2.setStarRating(3);
////            hotel2.setAveragePrice(120.00);
////            hotel2.setDiscount(0.05);
////            hotel2.setDescription("A cozy inn by the river, perfect for a relaxing getaway. Pet-friendly options available.");
////            hotel2.setEmail("contact@riversideinn.com");
////            hotel2.setMobile("987-654-3210");
////            hotel2.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Riverside+Inn");
////            hotel2.setTimesBooked(80);
////            hotel2.setAmenities(new HashSet<>(Arrays.asList(parking, wifi, petsAllowed)));
////            hotel2 = hotelRepository.save(hotel2);
////            
////
////            // --- 3. Create and Save 50 Hotels ---
////            List<Hotel> hotels = new ArrayList<>();
////            String[] commonHotelNames = {"Grand", "Majestic", "Royal", "Downtown", "Riverside", "City Center", "Inn", "Plaza", "Heights", "Bayview"};
////            String[] adjectives = {"Comfort", "Luxury", "Elite", "Prime", "Cornerstone", "Metropolitan", "Harmony", "Azure", "Summit", "Evergreen"};
////            String[] locations = {"Hotel", "Suites", "Lodge", "Resort", "Motel", "Boutique", "Palace"};
////            
////
////            String[] usCities = {
////                    "New York City, New York", "Los Angeles, California", "Chicago, Illinois",
////                    "Houston, Texas", "Phoenix, Arizona", "Philadelphia, Pennsylvania",
////                    "San Antonio, Texas", "San Diego, California", "Dallas, Texas",
////                    "San Jose, California", "Austin, Texas", "Jacksonville, Florida",
////                    "Fort Worth, Texas", "Columbus, Ohio", "Charlotte, North Carolina",
////                    "Indianapolis, Indiana", "San Francisco, California", "Seattle, Washington",
////                    "Denver, Colorado", "Washington, D.C.", "Boston, Massachusetts",
////                    "El Paso, Texas", "Nashville, Tennessee", "Detroit, Michigan",
////                    "Oklahoma City, Oklahoma", "Portland, Oregon", "Las Vegas, Nevada",
////                    "Memphis, Tennessee", "Louisville, Kentucky", "Baltimore, Maryland",
////                    "Milwaukee, Wisconsin", "Albuquerque, New Mexico", "Tucson, Arizona",
////                    "Fresno, California", "Sacramento, California", "Kansas City, Missouri",
////                    "Mesa, Arizona", "Atlanta, Georgia", "Long Beach, California",
////                    "Colorado Springs, Colorado", "Raleigh, North Carolina", "Miami, Florida",
////                    "Virginia Beach, Virginia", "Omaha, Nebraska", "Oakland, California",
////                    "Minneapolis, Minnesota", "Tulsa, Oklahoma", "Arlington, Texas",
////                    "New Orleans, Louisiana", "Wichita, Kansas"
////            };
////
////            List<String> houstonCities = new ArrayList<>(Collections.nCopies(8, "Houston, Texas"));
////            List<String> otherCities = new ArrayList<>();
////            for (String city : usCities) {
////                if (!city.equals("Houston, Texas")) {
////                    otherCities.add(city);
////                }
////            }
////
////            // Add 8 Houston hotels
////            for (int i = 0; i < 8; i++) {
////                hotels.add(createHotel(
////                        "Houston " + commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)],
////                        "Houston", "Texas", allAmenities, random
////                ));
////            }
////
////            // Add remaining hotels in other cities
////            for (int i = 0; i < 42; i++) { // 50 total - 8 Houston = 42
////                String[] cityState = otherCities.get(random.nextInt(otherCities.size())).split(", ");
////                hotels.add(createHotel(
////                        commonHotelNames[random.nextInt(commonHotelNames.length)] + " " + adjectives[random.nextInt(adjectives.length)] + " " + locations[random.nextInt(locations.length)] + " " + cityState[0],
////                        cityState[0], cityState[1], allAmenities, random
////                ));
////            }
////
////            for (Hotel hotel : hotels) {
////                hotelRepository.save(hotel);
////                // --- 4. Create and Save Hotel Rooms for each hotel ---
////                // Each hotel will have a mix of room types and quantities
////                int totalRoomsForHotel = 0;
////                for (RoomType rt : allRoomTypes) {
////                    int numberOfRooms = 5 + random.nextInt(10); // 5 to 14 rooms of each type
////                    float basePrice = 50.0f + (rt.getTypeId() * 75.0f) + random.nextFloat() * 50.0f; // Varies by room type
////                    float discount = random.nextBoolean() ? random.nextFloat() * 0.1f : 0.0f; // 0% to 10% discount
////
////                    HotelRoom hotelRoom = createHotelRoom(hotel, rt, numberOfRooms, basePrice, discount, allAmenities, random);
////                    hotelRoomRepository.save(hotelRoom);
//////                    hotel.getHotelRooms().add(hotelRoom);
////                    
//////                    hotel.addHotelRoom(hotelRoom); // Link room to hotel
////                    
//////                    hotelRepository.save(hotel);
////                    totalRoomsForHotel += numberOfRooms;
////                }
////                hotelRepository.save(hotel); // Re-save hotel to persist room associations
////                System.out.println("Created " + totalRoomsForHotel + " rooms for " + hotel.getHotelName());
////            }
////            System.out.println("Hotel and Room data populated for " + hotels.size() + " hotels.");
////            
////
////
////
////            // --- Fetch users for relationships ---
////            Optional<User> user1Opt = userRepository.findByUsername("user");
////            Optional<User> adminOpt = userRepository.findByUsername("admin");
////            Optional<User> testUser1Opt = userRepository.findByUsername("testuser1");
////            Optional<User> testUser2Opt = userRepository.findByUsername("testuser2");
////            Optional<User> testUser3Opt = userRepository.findByUsername("testuser3");
////            Optional<User> testUser4Opt = userRepository.findByUsername("testuser4");
////            Optional<User> testUser5Opt = userRepository.findByUsername("testuser5");
////
////            User user1 = user1Opt.orElse(null);
////            User admin = adminOpt.orElse(null);
////            User testUser1 = testUser1Opt.orElse(null);
////            User testUser2 = testUser2Opt.orElse(null);
////            User testUser3 = testUser3Opt.orElse(null);
////            User testUser4 = testUser4Opt.orElse(null);
////            User testUser5 = testUser5Opt.orElse(null);
//////
////////            // --- 5. Create and Save Loyalty Programs ---
////////            LoyaltyProgram bronze = new LoyaltyProgram("BRONZE", "Entry-level tier", 0, 0.0);
////////            LoyaltyProgram silver = new LoyaltyProgram("SILVER", "Achieved after 500 points, 5% discount.", 500, 0.05);
////////            LoyaltyProgram gold = new LoyaltyProgram("GOLD", "Achieved after 1500 points, 10% discount and late checkout.", 1500, 0.10);
////////
////////            bronze = loyaltyProgramRepository.save(bronze);
////////            silver = loyaltyProgramRepository.save(silver);
////////            gold = loyaltyProgramRepository.save(gold);
////////            System.out.println("Loyalty Programs populated.");
////////
////////            // --- 6. Create and Save User Loyalty Info ---
////////            if (user1 != null) {
////////                userLoyaltyRepository.save(new UserLoyalty(user1, 100, bronze));
////////            }
////////            if (testUser1 != null) {
////////                userLoyaltyRepository.save(new UserLoyalty(testUser1, 600, silver));
////////            }
////////            if (testUser2 != null) {
////////                userLoyaltyRepository.save(new UserLoyalty(testUser2, 1600, gold));
////////            }
////////            System.out.println("User Loyalty data populated.");
////////
////////            // --- 7. Create and Save User Preferences ---
////////            if (user1 != null) {
////////                UserPreference pref1 = new UserPreference(user1);
////////                pref1.setPreferredRoomType("DELUXE");
////////                pref1.setDietaryRestrictions("Vegetarian");
////////                pref1.setCommunicationPreference("EMAIL");
////////                userPreferenceRepository.save(pref1);
////////            }
////////            if (testUser3 != null) {
////////                UserPreference pref3 = new UserPreference(testUser3);
////////                pref3.setPreferredRoomType("SUITE");
////////                pref3.setPreferredAmenities("Swimming Pool, Spa Services");
////////                pref3.setCommunicationPreference("SMS");
////////                userPreferenceRepository.save(pref3);
////////            }
////////            System.out.println("User Preference data populated.");
//////
////
////            // --- 8. Create and Save Bookings ---
////            LocalDate today = LocalDate.now();
////            if (user1 != null) {
////                // Past booking
////                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.minusDays(10), today.minusDays(7), 1, 2, 360.00, Booking.BookingStatus.COMPLETED));
////                // Current booking (checking availability)
////                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.plusDays(1), today.plusDays(3), 2, 4, 1080.00, Booking.BookingStatus.CONFIRMED)); // 2 rooms booked
////                // Future booking
////                bookingRepository.save(new Booking(user1, hotels.get(0).getHotelRooms().iterator().next(), today.plusMonths(1), today.plusMonths(1).plusDays(5), 1, 2, 1500.00, Booking.BookingStatus.PENDING));
////            }
////            if (testUser1 != null) {
////                // Future booking in a different hotel
////                bookingRepository.save(new Booking(testUser1, hotels.get(1).getHotelRooms().iterator().next(), today.plusWeeks(2), today.plusWeeks(2).plusDays(3), 1, 2, 270.00, Booking.BookingStatus.CONFIRMED));
////            }
////            System.out.println("Booking data populated.");
////
////            // --- 9. Create and Save Feedback ---
////            if (user1 != null) {
////                feedbackRepository.save(new Feedback(user1, hotels.get(0), Feedback.FeedbackType.COMPLAINT, "No hot water in the shower this morning."));
////                feedbackRepository.save(new Feedback(user1, hotels.get(1), Feedback.FeedbackType.PRAISE, "The breakfast at Riverside Inn was excellent!"));
////            }
////            if (testUser2 != null) {
////                feedbackRepository.save(new Feedback(testUser2, hotels.get(0), Feedback.FeedbackType.SUGGESTION, "Could add more vegan options to room service menu."));
////            }
////            System.out.println("Feedback data populated.");
////
////            // --- 10. Create and Save Service Requests ---
////            if (user1 != null) {
////                serviceRequestRepository.save(new ServiceRequest(user1, hotels.get(0).getHotelRooms().iterator().next(), ServiceRequest.RequestType.ROOM_SERVICE, "Coffee and croissant to room 101 at 8 AM."));
////                serviceRequestRepository.save(new ServiceRequest(user1, hotels.get(0).getHotelRooms().iterator().next(), ServiceRequest.RequestType.HOUSEKEEPING, "Please clean the room thoroughly."));
////            }
////            if (testUser3 != null) {
////                serviceRequestRepository.save(new ServiceRequest(testUser3, hotels.get(1).getHotelRooms().iterator().next(), ServiceRequest.RequestType.MAINTENANCE, "Light bulb in bathroom is out."));
////            }
////            System.out.println("Service Request data populated.");
////
////            // --- 11. Create and Save Facility Reservations ---
////            if (user1 != null) {
////                facilityReservationRepository.save(new FacilityReservation(user1, hotels.get(0), FacilityReservation.FacilityType.SPA, today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
////            }
////            if (testUser4 != null) {
////                facilityReservationRepository.save(new FacilityReservation(testUser4, hotels.get(0), FacilityReservation.FacilityType.GYM, today, LocalTime.of(7, 30), LocalTime.of(8, 30)));
////            }
////            System.out.println("Facility Reservation data populated.");
////
//////            // --- 12. Create and Save Offers ---
//////            Offer summerSale = new Offer("SUMMER25", "Summer Holiday Sale", "25% off all bookings for July, perfect for your summer getaway!", Offer.DiscountType.PERCENTAGE, 0.25,
//////                    LocalDate.of(2025, 6, 10), LocalDate.of(2025, 7, 31));
//////            Offer weekendGetaway = new Offer("WEEKEND100", "Weekend Escape Deal", "Flat $100 off on weekend stays, relax and unwind!", Offer.DiscountType.FIXED_AMOUNT, 100.0,
//////                    LocalDate.of(2025, 6, 15), LocalDate.of(2025, 8, 31));
//////            Offer goldMemberDeal = new Offer("GOLDEXTRA", "Exclusive Gold Member Discount", "Additional 5% off for Gold members on any booking!", Offer.DiscountType.PERCENTAGE, 0.05,
//////                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 9, 30));
//////            goldMemberDeal.setTargetLoyaltyTier("GOLD");
//////
//////            Offer minBookingOffer = new Offer("LARGEBOOKING", "Big Booking Bonus", "15% off for bookings over $500, the more you spend, the more you save!", Offer.DiscountType.PERCENTAGE, 0.15,
//////                    LocalDate.of(2025, 6, 12), LocalDate.of(2025, 7, 15));
//////            minBookingOffer.setMinBookingAmount(500.0);
//////
//////            Offer repeatGuestOffer = new Offer("REPEAT20", "Welcome Back Discount", "20% off for our valued guests with 3 or more previous bookings!", Offer.DiscountType.PERCENTAGE, 0.20,
//////                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 1));
//////            repeatGuestOffer.setMinBookingsRequired(3);
//////
//////            // Add an expired offer to demonstrate filtering
//////            Offer expiredOffer = new Offer("EXPIRED10", "Early Bird Special (Expired)", "10% off for early birds, this offer is no longer valid.", Offer.DiscountType.PERCENTAGE, 0.10,
//////                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31));
//////            expiredOffer.setActive(false);
//////
//////
//////            summerSale = offerRepository.save(summerSale);
//////            weekendGetaway = offerRepository.save(weekendGetaway);
//////            goldMemberDeal = offerRepository.save(goldMemberDeal);
//////            minBookingOffer = offerRepository.save(minBookingOffer);
//////            repeatGuestOffer = offerRepository.save(repeatGuestOffer);
//////            expiredOffer = offerRepository.save(expiredOffer);
//////            System.out.println("Offer data populated.");
////
////
////            // --- 13. Populate Vector Store with Documents ---
////            List<Document> documents = new ArrayList<>();
////
////            // Add hotel descriptions to documents
////            for (Hotel hotel : hotels) {
////                documents.add(Document.builder()
////                        .text("Hotel Name: " + hotel.getHotelName() + ". Description: " + hotel.getDescription() + ". Amenities: " +
////                                hotel.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) +
////                                ". Address: " + hotel.getAddress() + ", " + hotel.getCity() + ", " + hotel.getState() +
////                                ". Star Rating: " + hotel.getStarRating() + ".")
////                        .metadata(Map.of("type", "hotel_description", "source_id", hotel.getHotelId()))
////                        .build());
////
////                // Add hotel room descriptions to documents
////                for (HotelRoom room : hotel.getHotelRooms()) {
////                    documents.add(Document.builder()
////                            .text("Room " + room.getType().getName() + " description: " + room.getDescription() +
////                                    ". Price: $" + String.format("%.2f", room.getPrice()) +
////                                    ". Amenities: " + room.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) +
////                                    ". Policies: " + room.getPolicies() +
////                                    ". Associated Hotel: " + room.getHotel().getHotelName() + ".")
////                            .metadata(Map.of("type", "room_description", "source_id", room.getHotelRoomId()))
////                            .build());
////                }
////            }
////
////            // Add amenity descriptions to documents
////            for (Amenities amenity : allAmenities) {
////                documents.add(Document.builder()
////                        .text("Amenity: " + amenity.getName() + " is available and typically includes " + getAmenityDescription(amenity.getName()) + ".")
////                        .metadata(Map.of("type", "amenity_info", "source_id", amenity.getA_id()))
////                        .build());
////            }
////
//////            // Add new documents for Offers (relevant for personalized offers/search)
//////            documents.add(Document.builder()
//////                    .text("Offer: " + summerSale.getTitle() + ". Code: " + summerSale.getOfferCode() + ". Description: " + summerSale.getDescription() + ". Valid from " + summerSale.getValidFrom() + " to " + summerSale.getValidTo() + ".")
//////                    .metadata(Map.of("type", "offer_description", "source_id", summerSale.getOfferId(), "offer_code", summerSale.getOfferCode(), "isActive", summerSale.isActive()))
//////                    .build());
//////            documents.add(Document.builder()
//////                    .text("Offer: " + weekendGetaway.getTitle() + ". Code: " + weekendGetaway.getOfferCode() + ". Description: " + weekendGetaway.getDescription() + ". Valid from " + weekendGetaway.getValidFrom() + " to " + weekendGetaway.getValidTo() + ".")
//////                    .metadata(Map.of("type", "offer_description", "source_id", weekendGetaway.getOfferId(), "offer_code", weekendGetaway.getOfferCode(), "isActive", weekendGetaway.isActive()))
//////                    .build());
//////            documents.add(Document.builder()
//////                    .text("Offer: " + goldMemberDeal.getTitle() + ". Code: " + goldMemberDeal.getOfferCode() + ". Description: " + goldMemberDeal.getDescription() + ". Target: " + goldMemberDeal.getTargetLoyaltyTier() + " members. Valid from " + goldMemberDeal.getValidFrom() + " to " + goldMemberDeal.getValidTo() + ".")
//////                    .metadata(Map.of("type", "offer_description", "source_id", goldMemberDeal.getOfferId(), "offer_code", goldMemberDeal.getOfferCode(), "target_loyalty", goldMemberDeal.getTargetLoyaltyTier(), "isActive", goldMemberDeal.isActive()))
//////                    .build());
//////            documents.add(Document.builder()
//////                    .text("Offer: " + minBookingOffer.getTitle() + ". Code: " + minBookingOffer.getOfferCode() + ". Description: " + minBookingOffer.getDescription() + ". Requires minimum booking amount: $" + minBookingOffer.getMinBookingAmount() + ". Valid from " + minBookingOffer.getValidFrom() + " to " + minBookingOffer.getValidTo() + ".")
//////                    .metadata(Map.of("type", "offer_description", "source_id", minBookingOffer.getOfferId(), "offer_code", minBookingOffer.getOfferCode(), "min_booking_amount", minBookingOffer.getMinBookingAmount(), "isActive", minBookingOffer.isActive()))
//////                    .build());
//////            documents.add(Document.builder()
//////                    .text("Offer: " + repeatGuestOffer.getTitle() + ". Code: " + repeatGuestOffer.getOfferCode() + ". Description: " + repeatGuestOffer.getDescription() + ". Requires minimum " + repeatGuestOffer.getMinBookingsRequired() + " previous bookings. Valid from " + repeatGuestOffer.getValidFrom() + " to " + repeatGuestOffer.getValidTo() + ".")
//////                    .metadata(Map.of("type", "offer_description", "source_id", repeatGuestOffer.getOfferId(), "offer_code", repeatGuestOffer.getOfferCode(), "min_bookings_required", repeatGuestOffer.getMinBookingsRequired(), "isActive", repeatGuestOffer.isActive()))
//////                    .build());
//////            documents.add(Document.builder()
//////                    .text("Offer: " + expiredOffer.getTitle() + ". Code: " + expiredOffer.getOfferCode() + ". Description: " + expiredOffer.getDescription() + ". Valid from " + expiredOffer.getValidFrom() + " to " + expiredOffer.getValidTo() + ". This offer is expired and inactive.")
//////                    .metadata(Map.of("type", "offer_description", "source_id", expiredOffer.getOfferId(), "offer_code", expiredOffer.getOfferCode(), "isActive", expiredOffer.isActive()))
//////                    .build());
////
////
////            // Log documents before adding to vector store
////            System.out.println("DEBUG: Documents prepared for vector store:");
////            documents.forEach(doc -> System.out.println("- " + doc.getText() + " (Metadata: " + doc.getMetadata() + ")"));
////
////            // Add documents to the vector store
////            vectorStore.add(documents);
////
////            System.out.println("Data initialization complete. Vector store populated with " + documents.size() + " documents.");
////        };
////    }
////
////    // --- Helper Methods for Data Generation ---
////
////    private Hotel createHotel(String name, String city, String state, List<Amenities> allAmenities, Random random) {
////        Hotel hotel = new Hotel();
////        hotel.setHotelName(name);
////        hotel.setAddress(random.nextInt(999) + 1 + " " + (new String[]{"Oak", "Pine", "Maple", "Elm", "Cedar"})[random.nextInt(5)] + " St");
////        hotel.setCity(city);
////        hotel.setState(state);
////        hotel.setStarRating(3 + random.nextInt(3)); // 3 to 5 stars
////        hotel.setAveragePrice(100.00 + random.nextInt(200) + random.nextDouble()); // 100-300
////        hotel.setDiscount(random.nextBoolean() ? random.nextDouble() * 0.15 : 0.0); // 0-15% discount
////        hotel.setDescription("Discover " + name + ", a premier " + hotel.getStarRating() + "-star hotel in " + city + ", " + state + ". " +
////                "Enjoy modern amenities and exceptional service for a memorable stay.");
////        hotel.setEmail("info@" + name.toLowerCase().replaceAll(" ", "") + ".com");
////        hotel.setMobile(String.format("%03d-%03d-%04d", random.nextInt(1000), random.nextInt(1000), random.nextInt(10000)));
////        hotel.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=" + name.replaceAll(" ", "+"));
////        hotel.setTimesBooked(random.nextInt(500)); // Up to 500 bookings
////
////        // Assign a random subset of amenities
////        Set<Amenities> assignedAmenities = new HashSet<>();
////        int numAmenities = 3 + random.nextInt(5); // 3 to 7 amenities per hotel
////        for (int i = 0; i < numAmenities; i++) {
////            assignedAmenities.add(allAmenities.get(random.nextInt(allAmenities.size())));
////        }
////        hotel.setAmenities(assignedAmenities);
////        return hotel;
////    }
////
////    private HotelRoom createHotelRoom(Hotel hotel, RoomType roomType, int noRooms, float price, float discount, List<Amenities> allAmenities, Random random) {
////        HotelRoom room = new HotelRoom();
////        room.setHotel(hotel);
////        room.setType(roomType);
////        room.setNoRooms(noRooms);
////        room.setPrice(price);
////        room.setDiscount(discount);
////        room.setDescription(roomType.getName() + " room with " + (random.nextBoolean() ? "a king-size bed" : "two queen beds") + ". " +
////                (random.nextBoolean() ? "Enjoy city views." : "Features a spacious layout."));
////        room.setPolicies("No smoking. Check-in 3 PM, Check-out 11 AM. " + (random.nextBoolean() ? "Complimentary cancellation up to 24 hours prior." : "Flexible booking options."));
////
////        Set<Amenities> roomAmenities = new HashSet<>();
////        roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Free Wi-Fi")).findFirst().orElse(null)); // Always add Wi-Fi
////        if (random.nextBoolean()) {
////            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Complimentary Breakfast")).findFirst().orElse(null));
////        }
////        if (roomType.getName().equals("Suite") && random.nextBoolean()) {
////            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Room Service")).findFirst().orElse(null));
////        }
////        if (hotel.getAmenities().contains(allAmenities.stream().filter(a -> a.getName().equals("Pets Allowed")).findFirst().orElse(null)) && random.nextBoolean()) {
////            roomAmenities.add(allAmenities.stream().filter(a -> a.getName().equals("Pets Allowed")).findFirst().orElse(null));
////        }
////        roomAmenities.remove(null); // Remove any nulls if amenity not found
////        room.setAmenities(roomAmenities);
////        return room;
////    }
////
////    private String getAmenityDescription(String amenityName) {
////        switch (amenityName) {
////            case "Parking":
////                return "on-site parking facilities, valet service, or nearby parking options";
////            case "Free Wi-Fi":
////                return "high-speed internet access throughout the property and in all rooms";
////            case "Swimming Pool":
////                return "indoor or outdoor pools, sometimes with hot tubs or poolside service";
////            case "Complimentary Breakfast":
////                return "a breakfast buffet or continental breakfast included with your stay";
////            case "Fitness Center":
////                return "gym access with cardio equipment, weights, and sometimes personal trainers";
////            case "Bar & Lounge":
////                return "an on-site bar serving drinks and light snacks, often with a relaxed atmosphere";
////            case "Spa Services":
////                return "massages, facials, and other wellness treatments available for booking";
////            case "Pets Allowed":
////                return "pet-friendly rooms and amenities, though fees or restrictions may apply";
////            case "24-Hour Front Desk":
////                return "round-the-clock assistance for check-in, check-out, and guest inquiries";
////            case "Restaurant":
////                return "dining options available on-site, serving lunch and dinner";
////            case "Concierge Service":
////                return "personalized assistance for reservations, tours, and local recommendations";
////            case "Business Center":
////                return "facilities for printing, scanning, and internet access for business needs";
////            case "Laundry Service":
////                return "on-site laundry facilities or professional laundry and dry cleaning services";
////            case "Room Service":
////                return "in-room dining options available during specific hours";
////            case "Family Rooms":
////                return "larger rooms or connecting rooms suitable for families, often with extra beds or amenities for children";
////            default:
////                return "general services and facilities";
////        }
////    }
////}
//
//
////package com.hotel.chatbox;
////
////import com.hotel.chatbox.domain.Amenities;
////import com.hotel.chatbox.domain.Hotel;
////import com.hotel.chatbox.domain.HotelRoom;
////import com.hotel.chatbox.domain.RoomType;
////import com.hotel.chatbox.domain.User;
////import com.hotel.chatbox.repository.AmenitiesRepository;
////import com.hotel.chatbox.repository.HotelRepository;
////import com.hotel.chatbox.repository.HotelRoomRepository;
////import com.hotel.chatbox.repository.RoomTypeRepository;
////import com.hotel.chatbox.repository.UserRepository;
////
////import org.springframework.boot.CommandLineRunner;
////import org.springframework.boot.SpringApplication;
////import org.springframework.boot.autoconfigure.SpringBootApplication;
////import org.springframework.context.annotation.Bean;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.transaction.annotation.Transactional;
////
////import org.springframework.ai.document.Document;
////import org.springframework.ai.vectorstore.VectorStore;
////
////import java.util.Arrays;
////import java.util.HashSet;
////import java.util.List;
////import java.util.Set;
////import java.util.stream.Collectors;
////import java.util.Map;
////import java.util.Optional;
////
////@SpringBootApplication
////public class HotelChatBoxApplication {
////
////	public static void main(String[] args) {
////		SpringApplication.run(HotelChatBoxApplication.class, args);
////	}
////
////	@Bean
////	public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
////		return args -> {
////			if (userRepository.findByUsername("user").isEmpty()) {
////				User user = new User();
////				user.setUsername("user");
////				user.setPassword(passwordEncoder.encode("password"));
////				user.setRole("USER");
////				userRepository.save(user);
////				System.out.println("Created user: user/password");
////			}
////
////			if (userRepository.findByUsername("admin").isEmpty()) {
////				User admin = new User();
////				admin.setUsername("admin");
////				admin.setPassword(passwordEncoder.encode("adminpass"));
////				admin.setRole("ADMIN");
////				userRepository.save(admin);
////				System.out.println("Created admin: admin/adminpass");
////			}
////		};
////	}
////
////	@Bean
////	@Transactional
////	public CommandLineRunner dataInitializer(
////			HotelRepository hotelRepository,
////			RoomTypeRepository roomTypeRepository,
////			AmenitiesRepository amenitiesRepository,
////			HotelRoomRepository hotelRoomRepository,
////			VectorStore vectorStore) {
////		return args -> {
////			System.out.println("Initializing data and vector store...");
////
////			// --- 1. Create and Save Amenities (with idempotency check) ---
////			Amenities parking = amenitiesRepository.findByName("Parking")
////					.orElseGet(() -> {
////						Amenities p = new Amenities();
////						p.setA_id(1);
////						p.setName("Parking");
////						System.out.println("Creating Amenity: Parking");
////						return amenitiesRepository.save(p);
////					});
////			Amenities wifi = amenitiesRepository.findByName("Free Wi-Fi")
////					.orElseGet(() -> {
////						Amenities w = new Amenities();
////						w.setA_id(2);
////						w.setName("Free Wi-Fi");
////						System.out.println("Creating Amenity: Free Wi-Fi");
////						return amenitiesRepository.save(w);
////					});
////			Amenities pool = amenitiesRepository.findByName("Swimming Pool")
////					.orElseGet(() -> {
////						Amenities p = new Amenities();
////						p.setA_id(3);
////						p.setName("Swimming Pool");
////						System.out.println("Creating Amenity: Swimming Pool");
////						return amenitiesRepository.save(p);
////					});
////			Amenities breakfast = amenitiesRepository.findByName("Complimentary Breakfast")
////					.orElseGet(() -> {
////						Amenities b = new Amenities();
////						b.setA_id(4);
////						b.setName("Complimentary Breakfast");
////						System.out.println("Creating Amenity: Complimentary Breakfast");
////						return amenitiesRepository.save(b);
////					});
////			Amenities fitness = amenitiesRepository.findByName("Fitness Center")
////					.orElseGet(() -> {
////						Amenities f = new Amenities();
////						f.setA_id(5);
////						f.setName("Fitness Center");
////						System.out.println("Creating Amenity: Fitness Center");
////						return amenitiesRepository.save(f);
////					});
////			Amenities bar = amenitiesRepository.findByName("Bar & Lounge")
////					.orElseGet(() -> {
////						Amenities b = new Amenities();
////						b.setA_id(6);
////						b.setName("Bar & Lounge");
////						System.out.println("Creating Amenity: Bar & Lounge");
////						return amenitiesRepository.save(b);
////					});
////			Amenities spa = amenitiesRepository.findByName("Spa Services")
////					.orElseGet(() -> {
////						Amenities s = new Amenities();
////						s.setA_id(7);
////						s.setName("Spa Services");
////						System.out.println("Creating Amenity: Spa Services");
////						return amenitiesRepository.save(s);
////					});
////			Amenities petsAllowed = amenitiesRepository.findByName("Pets Allowed")
////					.orElseGet(() -> {
////						Amenities p = new Amenities();
////						p.setA_id(8);
////						p.setName("Pets Allowed");
////						System.out.println("Creating Amenity: Pets Allowed");
////						return amenitiesRepository.save(p);
////					});
////
////
////			// --- 2. Create and Save Room Types (with idempotency check) ---
////			RoomType standard = roomTypeRepository.findByNameIgnoreCase("Standard")
////					.orElseGet(() -> {
////						RoomType rt = new RoomType();
////						rt.setTypeId(1);
////						rt.setName("Standard");
////						System.out.println("Creating RoomType: Standard");
////						return roomTypeRepository.save(rt);
////					});
////			RoomType deluxe = roomTypeRepository.findByNameIgnoreCase("Deluxe")
////					.orElseGet(() -> {
////						RoomType rt = new RoomType();
////						rt.setTypeId(2);
////						rt.setName("Deluxe");
////						System.out.println("Creating RoomType: Deluxe");
////						return roomTypeRepository.save(rt);
////					});
////			RoomType suite = roomTypeRepository.findByNameIgnoreCase("Suite")
////					.orElseGet(() -> {
////						RoomType rt = new RoomType();
////						rt.setTypeId(3);
////						rt.setName("Suite");
////						System.out.println("Creating RoomType: Suite");
////						return roomTypeRepository.save(rt);
////					});
////
////
////			// --- 3. Create and Save Hotels (with idempotency check) ---
////			Hotel tempHotel1 = hotelRepository.findByHotelNameContainingIgnoreCase("Grand Plaza Hotel")
////					.orElseGet(() -> {
////						Hotel h = new Hotel();
////						h.setHotelName("Grand Plaza Hotel");
////						h.setAddress("123 Main St");
////						h.setCity("New York City");
////						h.setState("New York");
////						h.setStarRating(5);
////						h.setAveragePrice(250.00);
////						h.setDiscount(0.10);
////						h.setDescription("Experience luxury and comfort at our downtown hotel with stunning city views.");
////						h.setEmail("info@grandplaza.com");
////						h.setMobile("123-456-7890");
////						h.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Grand+Plaza");
////						h.setTimesBooked(150);
////						h.setAmenities(Set.of(parking, wifi, pool, breakfast, fitness, bar));
////						System.out.println("Creating Hotel: Grand Plaza Hotel");
////						return hotelRepository.save(h);
////					});
////
////			Hotel tempHotel2 = hotelRepository.findByHotelNameContainingIgnoreCase("Riverside Inn")
////					.orElseGet(() -> {
////						Hotel h = new Hotel();
////						h.setHotelName("Riverside Inn");
////						h.setAddress("45 River Rd");
////						h.setCity("Austin");
////						h.setState("Texas");
////						h.setStarRating(3);
////						h.setAveragePrice(120.00);
////						h.setDiscount(0.05);
////						h.setDescription("A cozy inn by the river, perfect for a relaxing getaway. Pet-friendly options available.");
////						h.setEmail("contact@riversideinn.com");
////						h.setMobile("987-654-3210");
////						h.setImageURL("https://placehold.co/600x400/000000/FFFFFF?text=Riverside+Inn");
////						h.setTimesBooked(80);
////						h.setAmenities(Set.of(parking, wifi, petsAllowed));
////						System.out.println("Creating Hotel: Riverside Inn");
////						return hotelRepository.save(h);
////					});
////
////			// Re-fetch hotels to ensure they are fully managed within the current transaction
////			// We still do this to be safe, but the critical change is in how HotelRoom is handled.
////			final Hotel hotel1 = hotelRepository.findById(tempHotel1.getHotelId())
////					.orElseThrow(() -> new IllegalStateException("Hotel 1 not found after creation/retrieval!"));
////			final Hotel hotel2 = hotelRepository.findById(tempHotel2.getHotelId())
////					.orElseThrow(() -> new IllegalStateException("Hotel 2 not found after creation/retrieval!"));
////
////
////			// --- 4. Create and Save Hotel Rooms (with idempotency check) ---
////			// The key change is to save the HotelRoom first, which establishes the FK.
////			// Then, add it to the Hotel's collection to maintain object graph consistency.
////
////			// Room 1_1: Grand Plaza Standard Room
////			HotelRoom room1_1 = hotelRoomRepository.findByHotelAndType(hotel1, standard)
////					.orElseGet(() -> {
////						HotelRoom hr = new HotelRoom();
////						hr.setHotel(hotel1); // Set the ManyToOne side
////						hr.setType(standard);
////						hr.setNoRooms(10);
////						hr.setPrice(180.00f);
////						hr.setDiscount(0.0f);
////						hr.setDescription("Comfortable standard room with a king-size bed and city view.");
////						hr.setPolicies("No smoking. Check-in 3 PM, Check-out 11 AM.");
////						hr.setAmenities(Set.of(wifi));
////						System.out.println("Creating HotelRoom: " + hotel1.getHotelName() + " - " + standard.getName());
////						// Crucial: Save the HotelRoom *before* adding to the Hotel's collection
////						return hotelRoomRepository.save(hr);
////					});
////			// Add to the Hotel's collection only after the HotelRoom is saved
////			hotel1.getHotelRooms().add(room1_1);
////
////
////			// Room 1_2: Grand Plaza Deluxe Room
////			HotelRoom room1_2 = hotelRoomRepository.findByHotelAndType(hotel1, deluxe)
////					.orElseGet(() -> {
////						HotelRoom hr = new HotelRoom();
////						hr.setHotel(hotel1);
////						hr.setType(deluxe);
////						hr.setNoRooms(5);
////						hr.setPrice(300.00f);
////						hr.setDiscount(0.05f);
////						hr.setDescription("Spacious deluxe room with a balcony overlooking the park, includes complimentary breakfast.");
////						hr.setPolicies("Check-in 3 PM, Check-out 11 AM.");
////						hr.setAmenities(Set.of(wifi, breakfast));
////						System.out.println("Creating HotelRoom: " + hotel1.getHotelName() + " - " + deluxe.getName());
////						return hotelRoomRepository.save(hr);
////					});
////			hotel1.getHotelRooms().add(room1_2);
////
////
////			// Room 2_1: Riverside Inn Standard Room
////			HotelRoom room2_1 = hotelRoomRepository.findByHotelAndType(hotel2, standard)
////					.orElseGet(() -> {
////						HotelRoom hr = new HotelRoom();
////						hr.setHotel(hotel2);
////						hr.setType(standard);
////						hr.setNoRooms(12);
////						hr.setPrice(90.00f);
////						hr.setDiscount(0.0f);
////						hr.setDescription("Basic and affordable standard room with two queen beds, near the river.");
////						hr.setPolicies("Check-in 2 PM, Check-out 10 AM. Pets allowed with prior notification.");
////						hr.setAmenities(Set.of(wifi, petsAllowed));
////						System.out.println("Creating HotelRoom: " + hotel2.getHotelName() + " - " + standard.getName());
////						return hotelRoomRepository.save(hr);
////					});
////			hotel2.getHotelRooms().add(room2_1);
////
////
////			// Final save of hotels. This persists the collection changes.
////			// Because of `orphanRemoval=true` and `cascade=ALL`, these saves
////			// are crucial to ensure any changes to the `hotelRooms` collection
////			// are synchronized with the database.
////			hotelRepository.save(hotel1);
////			hotelRepository.save(hotel2);
////
////
////			// --- 5. Populate Vector Store with Documents ---
////			List<Document> documents = Arrays.asList(
////					Document.builder()
////					.text("Hotel Name: " + hotel1.getHotelName() + ". Description: " + hotel1.getDescription() + ". Amenities: " + hotel1.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + ". Address: " + hotel1.getAddress() + ", " + hotel1.getCity() + ", " + hotel1.getState() + ". Star Rating: " + hotel1.getStarRating() + ".")
////					.metadata(Map.of("type", "hotel_description", "source_id", hotel1.getHotelId()))
////					.build(),
////					Document.builder()
////					.text("Hotel Name: " + hotel2.getHotelName() + ". Description: " + hotel2.getDescription() + ". Amenities: " + hotel2.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + ". Address: " + hotel2.getAddress() + ", " + hotel2.getCity() + ", " + hotel2.getState() + ". Star Rating: " + hotel2.getStarRating() + ".")
////					.metadata(Map.of("type", "hotel_description", "source_id", hotel2.getHotelId()))
////					.build(),
////					Document.builder()
////					.text("Room " + room1_1.getType().getName() + " description: " + room1_1.getDescription() + ". Price: $" + room1_1.getPrice() + ". Amenities: " + room1_1.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + ". Policies: " + room1_1.getPolicies() + ". Associated Hotel: " + room1_1.getHotel().getHotelName() + ".")
////					.metadata(Map.of("type", "room_description", "source_id", room1_1.getHotelRoomId()))
////					.build(),
////					Document.builder()
////					.text("Room " + room1_2.getType().getName() + " description: " + room1_2.getDescription() + ". Price: $" + room1_2.getPrice() + ". Amenities: " + room1_2.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + ". Policies: " + room1_2.getPolicies() + ". Associated Hotel: " + room1_2.getHotel().getHotelName() + ".")
////					.metadata(Map.of("type", "room_description", "source_id", room1_2.getHotelRoomId()))
////					.build(),
////					Document.builder()
////					.text("Room " + room2_1.getType().getName() + " description: " + room2_1.getDescription() + ". Price: $" + room2_1.getPrice() + ". Amenities: " + room2_1.getAmenities().stream().map(Amenities::getName).collect(Collectors.joining(", ")) + ". Policies: " + room2_1.getPolicies() + ". Associated Hotel: " + room2_1.getHotel().getHotelName() + ".")
////					.metadata(Map.of("type", "room_description", "source_id", room2_1.getHotelRoomId()))
////					.build(),
////					Document.builder()
////					.text("Amenity: " + wifi.getName() + " is available.")
////					.metadata(Map.of("type", "amenity_info", "source_id", wifi.getA_id()))
////					.build(),
////					Document.builder()
////					.text("Amenity: " + pool.getName() + " is available.")
////					.metadata(Map.of("type", "amenity_info", "source_id", pool.getA_id()))
////					.build(),
////					Document.builder()
////					.text("Amenity: " + breakfast.getName() + " is available.")
////					.metadata(Map.of("type", "amenity_info", "source_id", breakfast.getA_id()))
////					.build()
////					);
////
////			System.out.println("DEBUG: Documents prepared for vector store:");
////			documents.forEach(doc -> System.out.println("- " + doc.getText() + " (Metadata: " + doc.getMetadata() + ")"));
////
////			vectorStore.add(documents);
////
////			System.out.println("Data initialization complete. Vector store populated with " + documents.size() + " documents.");
////		};
////	}
////}
//
//
//
//
