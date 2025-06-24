//package com.hotel.chatbox.parser;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.springframework.stereotype.Component;
//
//@Component
//public class HotelParser {
//
//    public static class HotelInfo {
//        private String name;
//        private int starRating;
//        private String amenities; // Stored as a single string for simplicity
//        private String fullParsedLine; // The original line that was parsed
//
//        public HotelInfo(String name, int starRating, String amenities, String fullParsedLine) {
//            this.name = name;
//            this.starRating = starRating;
//            this.amenities = amenities;
//            this.fullParsedLine = fullParsedLine;
//        }
//
//        // Getters for accessing the parsed data
//        public String getName() { return name; }
//        public int getStarRating() { return starRating; }
//        public String getAmenities() { return amenities; }
//        public String getFullParsedLine() { return fullParsedLine; }
//
//        @Override
//        public String toString() {
//            return "HotelInfo{" +
//                    "name='" + name + '\'' +
//                    ", starRating=" + starRating +
//                    ", amenities='" + amenities + '\'' +
//                    ", fullParsedLine='" + fullParsedLine + '\'' +
//                    '}';
//        }
//    }
//    
//    public boolean canBeParsed(String response) {
//    	return isNumberedListResponse(response) || isConversationalHotelMention(response);
//    	
//    }
//    
//
//    /**
//     * Main public method to parse various forms of AI responses containing hotel information.
//     * It attempts to determine the format (numbered list vs. conversational) and calls the appropriate parser.
//     *
//     * @param response The string response from the AI.
//     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
//     */
//    public List<HotelInfo> parse(String response) {
//        if (response == null || response.trim().isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        // Prioritize parsing numbered lists, as they have a more consistent structure
//        if (isNumberedListResponse(response)) {
//            return parseNumberedHotelList(response);
//        }
//        // If not a numbered list, try to parse conversational mentions
//        else if (isConversationalHotelMention(response)) {
//            return parseConversationalHotelInfo(response);
//        }
//        return new ArrayList<>();
//    }
//
//    /**
//     * Helper method to check if the response contains common hotel-related keywords.
//     */
//    private boolean hasHotelKeywords(String response) {
//        response = response.toLowerCase();
//        return response.contains("hotel") ||
//               response.contains("hotels") ||
//               response.contains("inn") ||
//               response.contains("resort") ||
//               response.contains("lodge") ||
//               response.contains("suites") || // Added for broader hotel types
//               response.contains("boutique") ||
//               response.contains("motel");    // Added for broader hotel types
//    }
//
//    /**
//     * Helper method to validate if the response is likely a numbered list of hotels.
//     */
//    private boolean isNumberedListResponse(String response) {
//        // Must contain hotel keywords
//        if (!hasHotelKeywords(response)) {
//            return false;
//        }
//        // Check for common indicators of a numbered list (e.g., "1.", " 2.")
//        Pattern numberedListPattern = Pattern.compile("^\\s*\\d+\\.\\s+", Pattern.MULTILINE);
//        return numberedListPattern.matcher(response).find();
//    }
//
//    /**
//     * Helper method to validate if the response contains conversational mentions of hotels
//     * that are not necessarily in a numbered list format.
//     */
//    private boolean isConversationalHotelMention(String response) {
//        // Must contain hotel keywords
//        if (!hasHotelKeywords(response)) {
//            return false;
//        }
//        // Look for patterns like "X, a Y-star hotel", "HotelName offers Z", "HotelName has Z"
//        Pattern conversationalPattern = Pattern.compile(
//            "\\b(?:hotel|inn|resort|lodge|suites|boutique|motel)\\b(?:,\\s*(?:a\\s*)?(?:premier\\s*)?\\d-star hotel)?(?:.*(?:offers|has|provides|features|includes))?",
//            Pattern.CASE_INSENSITIVE
//        );
//        return conversationalPattern.matcher(response).find();
//    }
//
//    /**
//     * Parses a string that is assumed to be a numbered list of hotels into a list of HotelInfo objects.
//     * This method relies on a flexible regular expression to handle variations in the numbered list format,
//     * including optional bolding, different star rating prefixes, and varied amenity introductions.
//     *
//     * @param response The validated string response from the AI, expected to be a numbered list.
//     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
//     */
//    public List<HotelInfo> parseNumberedHotelList(String response) {
//        List<HotelInfo> hotels = new ArrayList<>();
//        if (response == null || response.trim().isEmpty()) {
//            return hotels;
//        }
//
//        // Updated Regular expression to extract details from each hotel line in a numbered list.
//        // It's designed to be flexible for various formats seen in examples 1, 2, 3, 4, 6, and the new ones.
//        Pattern hotelLinePattern = Pattern.compile(
//            "^\\s*\\d+\\.\\s+" +                      // 1. Start of line, optional whitespace, list number
//            "(?:\\*\\*([^*]+?)\\*\\*|" +              // Group 1: BOLDED Hotel Name (e.g., "**Hotel Name**")
//            "([^*-]+?))" +                            // OR Group 2: NON-BOLDED Hotel Name (e.g., "Hotel Name")
//            "\\s*-\\s*" +                             // Separator (e.g., " - ")
//            "(?:(?:A|a)(?:\\s+premier)?\\s*)?" +      // Optional prefix for star rating (e.g., "A", "a", "A premier")
//            "(\\d+)-star hotel" +                     // Group 3: Star Rating Digit (e.g., "5") followed by "-star hotel"
//            "\\s*" +                                  // Optional whitespace
//            "(?:" +                                   // Start non-capturing group for flexible description before amenities
//                "(?:(?:offers|provides|features|includes|with|that includes|featuring|that also offers)\\s*" + // Various amenity intro verbs
//                "(?:modern\\s*)?amenities(?:\\s*and\\s*exceptional\\s*service)?(?:,\\s*(?:including|like))?" + // "modern amenities and exceptional service, with amenities like"
//                "|" +                                 // OR
//                "(?:available|facilities|which is|that is|in)\\s*" + // "parking available", "which is a 3-star hotel"
//            ")?" +                                    // End non-capturing group, make it entirely optional
//            "\\s*" +                                  // Optional whitespace
//            "(.+?)" +                                 // Group 4: The rest of the line (actual amenities/description), non-greedy
//            "(?:\\.|\\n|$)",                          // Ends with a period, newline, or end of string
//            Pattern.MULTILINE // Enable multi-line matching for ^ and $
//        );
//
//        Matcher matcher = hotelLinePattern.matcher(response);
//
//        while (matcher.find()) {
//            try {
//                String name = matcher.group(1); // Attempt to get bolded name first
//                if (name == null) {
//                    name = matcher.group(2); // If bolded is null, get non-bolded name
//                }
//                name = name.trim();
//
//                int starRating = Integer.parseInt(matcher.group(3));
//                String amenitiesRaw = matcher.group(4).trim();
//
//                // Call helper to clean up the raw amenities string
//                String amenities = extractAmenitiesFromDescription(amenitiesRaw);
//
//                hotels.add(new HotelInfo(name, starRating, amenities, matcher.group(0).trim()));
//
//            } catch (NumberFormatException e) {
//                System.err.println("Warning: Could not parse star rating for line: " + matcher.group(0) + " - " + e.getMessage());
//            } catch (Exception e) {
//                System.err.println("Warning: Error parsing hotel line: " + matcher.group(0) + " - " + e.getMessage());
//            }
//        }
//        return hotels;
//    }
//
//    /**
//     * Parses a string that contains conversational mentions of hotels (not in a numbered list format).
//     * This method is designed for single or multiple hotel mentions within general sentences,
//     * such as those in examples 5 and 7, and the new conversational example.
//     *
//     * @param response The string response from the AI, expected to be conversational.
//     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
//     */
//    public List<HotelInfo> parseConversationalHotelInfo(String response) {
//        List<HotelInfo> hotels = new ArrayList<>();
//        if (response == null || response.trim().isEmpty()) {
//            return hotels;
//        }
//
//        // Pattern for finding hotel mentions in conversational text.
//        // It tries to capture the hotel name, an optional star rating, and associated amenity descriptions.
//        Pattern conversationalHotelPattern = Pattern.compile(
//            "([A-Za-z0-9\\s]+(?:Hotel|Inn|Resort|Lodge|Suites|Boutique|Motel))" + // Group 1: Hotel Name (e.g., "Houston Grand Comfort Suites")
//            "(?:,\\s*(?:a\\s*)?(?:premier\\s*)?(\\d+)-star hotel)?" +             // Optional Group 2: Star rating (e.g., ", a 4-star hotel", "premier 3-star hotel")
//            "(?:\\s*(?:(?:and\\s*)?(?:as they both|that also|which is|It)?\\s*(?:have|offers|provides|features|includes)?\\s*(.+?)))?" + // Optional Group 3: Amenities/Description
//            "(?:\\.|\\n|\\s*and\\s*(?=[A-Z])|$)", // Lookahead for sentence end, newline, " and " followed by capitalized word (for next hotel), or string end
//            Pattern.CASE_INSENSITIVE
//        );
//
//        Matcher matcher = conversationalHotelPattern.matcher(response);
//
//        // Find all matches in the response
//        while (matcher.find()) {
//            try {
//                String name = matcher.group(1).trim();
//                int starRating = 0; // Default to 0 if not found
//                if (matcher.group(2) != null) {
//                    starRating = Integer.parseInt(matcher.group(2));
//                }
//
//                String amenities = ""; // Default to empty
//                if (matcher.group(3) != null) {
//                    amenities = matcher.group(3).trim();
//                    // Clean up common introductory phrases from amenities in conversational text
//                    amenities = amenities.replaceAll(
//                        "^(?:swimming pools and other amenities|swimming pool|parking as one of its amenities|modern amenities and exceptional service for a memorable stay|parking facilities|parking available|a great stay|in Houston, Texas)\\s*\\.?\\s*",
//                        ""
//                    );
//                    amenities = amenities.trim();
//                }
//
//                if (!name.isEmpty()) { // Only add if a valid hotel name was found
//                    hotels.add(new HotelInfo(name, starRating, amenities, matcher.group(0).trim()));
//                }
//            } catch (NumberFormatException e) {
//                System.err.println("Warning: Could not parse star rating in conversational line: " + matcher.group(0) + " - " + e.getMessage());
//            } catch (Exception e) {
//                System.err.println("Warning: Error parsing conversational hotel line: " + matcher.group(0) + " - " + e.getMessage());
//            }
//        }
//        return hotels;
//    }
//
//    /**
//     * Helper method to clean up the raw amenities string extracted by regex,
//     * removing common introductory phrases like "amenities, including".
//     */
//    private String extractAmenitiesFromDescription(String amenitiesRaw) {
//        String cleanedAmenities = amenitiesRaw;
//        // More comprehensive pattern for amenity introductory phrases
//        Pattern pattern = Pattern.compile(
//            "^(?:amenities(?:,\\s*(?:including|like))?|including|like|offering|featuring|that includes|with|that also offers|provides|has|offers|available|facilities)\\s*(.*)",
//            Pattern.CASE_INSENSITIVE
//        );
//        Matcher matcher = pattern.matcher(amenitiesRaw);
//        if (matcher.find()) {
//            cleanedAmenities = matcher.group(1).trim();
//        }
//
//        // Remove any trailing periods
//        if (cleanedAmenities.endsWith(".")) {
//            cleanedAmenities = cleanedAmenities.substring(0, cleanedAmenities.length() - 1);
//        }
//        return cleanedAmenities;
//    }
//}


package com.hotel.chatbox.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class HotelParser {

    public static class HotelInfo {
        private String name;
        private int starRating;
        private String amenities; // Stored as a single string for simplicity
        private String fullParsedLine; // The original line that was parsed

        public HotelInfo(String name, int starRating, String amenities, String fullParsedLine) {
            this.name = name;
            this.starRating = starRating;
            this.amenities = amenities;
            this.fullParsedLine = fullParsedLine;
        }

        // Getters for accessing the parsed data
        public String getName() { return name; }
        public int getStarRating() { return starRating; }
        public String getAmenities() { return amenities; }
        public String getFullParsedLine() { return fullParsedLine; }

        @Override
        public String toString() {
            return "HotelInfo{" +
                    "name='" + name + '\'' +
                    ", starRating=" + starRating +
                    ", amenities='" + amenities + '\'' +
                    ", fullParsedLine='" + fullParsedLine + '\'' +
                    '}';
        }
    }
    
    
    
    public boolean canBeParsed(String response) {
    	return isNumberedListResponse(response) || isConversationalHotelMention(response);
    	
    }

    /**
     * Helper method to validate if the response is likely a numbered list of hotels.
     */
    private boolean isNumberedListResponse(String response) {
        // Must contain hotel keywords
        if (!hasHotelKeywords(response)) {
            return false;
        }
        // Check for common indicators of a numbered list (e.g., "1.", " 2.")
        Pattern numberedListPattern = Pattern.compile("^\\s*\\d+\\.\\s+", Pattern.MULTILINE);
        return numberedListPattern.matcher(response).find();
    }

    /**
     * Helper method to validate if the response contains conversational mentions of hotels
     * that are not necessarily in a numbered list format.
     */
    private boolean isConversationalHotelMention(String response) {
        // Must contain hotel keywords
        if (!hasHotelKeywords(response)) {
            return false;
        }
        // Look for patterns like "X, a Y-star hotel", "HotelName offers Z", "HotelName has Z"
        // This is a heuristic and can be refined further if needed.
        Pattern conversationalPattern = Pattern.compile(
            "\\b(?:hotel|inn|resort|lodge|suites|boutique|motel)\\b(?:,\\s*a\\s*\\d-star hotel)?(?:.*(?:offers|has|provides|features|includes))?",
            Pattern.CASE_INSENSITIVE
        );
        return conversationalPattern.matcher(response).find();
    }
    
    
    /**
     * Helper method to check if the response contains common hotel-related keywords.
     */
    private boolean hasHotelKeywords(String response) {
        response = response.toLowerCase();
        return response.contains("hotel") ||
               response.contains("hotels") ||
               response.contains("inn") ||
               response.contains("resort") ||
               response.contains("lodge") ||
               response.contains("suites") ||
               response.contains("boutique") ||
               response.contains("motel");    
    }
    

    /**
     * Main public method to parse various forms of AI responses containing hotel information.
     * It attempts to determine the format (numbered list vs. conversational) and calls the appropriate parser.
     *
     * @param response The string response from the AI.
     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
     */
    public List<HotelInfo> parse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Prioritize parsing numbered lists, as they have a more consistent structure
        if (isNumberedListResponse(response)) {
            return parseNumberedHotelList(response);
        }
        // If not a numbered list, try to parse conversational mentions
        else if (isConversationalHotelMention(response)) {
            return parseConversationalHotelInfo(response);
        }
        return new ArrayList<>();
    }

    

    /**
     * Parses a string that is assumed to be a numbered list of hotels into a list of HotelInfo objects.
     * This method relies on a flexible regular expression to handle variations in the numbered list format,
     * including optional bolding, different star rating prefixes, and varied amenity introductions.
     *
     * @param response The validated string response from the AI, expected to be a numbered list.
     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
     */
    public List<HotelInfo> parseNumberedHotelList(String response) {
        List<HotelInfo> hotels = new ArrayList<>();
        if (response == null || response.trim().isEmpty()) {
            return hotels;
        }

        // Updated Regular expression to extract details from each hotel line in a numbered list.
        // It's designed to be flexible for various formats seen in examples 1, 2, 3, 4, 6.
        Pattern hotelLinePattern = Pattern.compile(
            "^\\s*\\d+\\.\\s+" +                      // 1. Start of line, optional whitespace, list number
            "(?:\\*\\*([^*]+?)\\*\\*|" +              // Group 1: BOLDED Hotel Name (e.g., "**Hotel Name**")
            "([^*-]+?))" +                            // OR Group 2: NON-BOLDED Hotel Name (e.g., "Hotel Name")
            "\\s*-\\s*" +                             // Separator (e.g., " - ")
            "(?:(?:A|a)(?:\\s+premier)?\\s*)?" +      // Optional prefix for star rating (e.g., "A", "a", "A premier")
            "(\\d+)-star hotel" +                     // Group 3: Star Rating Digit (e.g., "5") followed by "-star hotel"
            "\\s*" +                                  // Optional whitespace
            "(?:" +                                   // Start non-capturing group for amenity phrases
                "(?:with|that includes|offering|featuring|that also offers|with amenities|provides|has|offers)" + // Various amenity intro phrases
                "(?:modern\\s*)?" +                   // Optional "modern"
                "(?:amenities(?:,\\s*(?:including|like))?)?" + // Optional "amenities, including/like"
                "|" +                                 // OR
                "(?:available|facilities)" +          // Simple amenity mentions like "parking available"
            ")?" +                                    // End non-capturing group, make it entirely optional
            "\\s*" +                                  // Optional whitespace
            "(.+?)" +                                 // Group 4: The rest of the line (amenities/description), non-greedy
            "(?:\\.|\\n|$)",                          // Ends with a period, newline, or end of string
            Pattern.MULTILINE // Enable multi-line matching for ^ and $
        );

        Matcher matcher = hotelLinePattern.matcher(response);

        while (matcher.find()) {
            try {
                String name = matcher.group(1); // Attempt to get bolded name first
                if (name == null) {
                    name = matcher.group(2); // If bolded is null, get non-bolded name
                }
                name = name.trim();

                int starRating = Integer.parseInt(matcher.group(3));
                String amenitiesRaw = matcher.group(4).trim();

                // Call helper to clean up the raw amenities string
                String amenities = extractAmenitiesFromDescription(amenitiesRaw);

                hotels.add(new HotelInfo(name, starRating, amenities, matcher.group(0).trim()));

            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse star rating for line: " + matcher.group(0) + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Warning: Error parsing hotel line: " + matcher.group(0) + " - " + e.getMessage());
            }
        }
        return hotels;
    }

    /**
     * Parses a string that contains conversational mentions of hotels (not in a numbered list format).
     * This method is designed for single or multiple hotel mentions within general sentences,
     * such as those in examples 5 and 7.
     *
     * @param response The string response from the AI, expected to be conversational.
     * @return A list of HotelInfo objects, potentially empty if no hotels could be parsed.
     */
    public List<HotelInfo> parseConversationalHotelInfo(String response) {
        List<HotelInfo> hotels = new ArrayList<>();
        if (response == null || response.trim().isEmpty()) {
            return hotels;
        }

        // Pattern for finding hotel mentions in conversational text.
        // It tries to capture the hotel name, an optional star rating, and associated amenity descriptions.
        Pattern conversationalHotelPattern = Pattern.compile(
            "([A-Za-z0-9\\s]+(?:Hotel|Inn|Resort|Lodge|Suites|Boutique|Motel))" + // Group 1: Hotel Name (e.g., "Houston Grand Comfort Suites")
            "(?:,\\s*a\\s*(\\d+)-star hotel)?" +                                  // Optional Group 2: Star rating (e.g., ", a 4-star hotel")
            "(?:\\s*(?:(?:and\\s*)?(?:as they both|that also)?\\s*(?:have|offers|provides|features|includes)\\s*(.+?)))?" + // Optional Group 3: Amenities/Description
            "(?:\\.|\\n|\\s*and\\s*(?=[A-Z])|$)", // Lookahead for sentence end, newline, " and " followed by capitalized word (for next hotel), or string end
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = conversationalHotelPattern.matcher(response);

        // Find all matches in the response
        while (matcher.find()) {
            try {
                String name = matcher.group(1).trim();
                int starRating = 0; // Default to 0 if not found
                if (matcher.group(2) != null) {
                    starRating = Integer.parseInt(matcher.group(2));
                }

                String amenities = ""; // Default to empty
                if (matcher.group(3) != null) {
                    amenities = matcher.group(3).trim();
                    // Clean up common introductory phrases from amenities in conversational text
                    amenities = amenities.replaceAll(
                        "^(?:swimming pools and other amenities|swimming pool|parking as one of its amenities|modern amenities for a great stay|parking facilities|parking available|a great stay)\\s*",
                        ""
                    );
                    amenities = amenities.trim();
                }

                if (!name.isEmpty()) { // Only add if a valid hotel name was found
                    hotels.add(new HotelInfo(name, starRating, amenities, matcher.group(0).trim()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse star rating in conversational line: " + matcher.group(0) + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Warning: Error parsing conversational hotel line: " + matcher.group(0) + " - " + e.getMessage());
            }
        }
        return hotels;
    }

    /**
     * Helper method to clean up the raw amenities string extracted by regex,
     * removing common introductory phrases like "amenities, including".
     */
    private String extractAmenitiesFromDescription(String amenitiesRaw) {
        String cleanedAmenities = amenitiesRaw;
        // More comprehensive pattern for amenity introductory phrases
        Pattern pattern = Pattern.compile(
            "^(?:amenities(?:,\\s*(?:including|like))?|including|like|offering|featuring|that includes|with|that also offers|provides|has|offers|available|facilities)\\s*(.*)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(amenitiesRaw);
        if (matcher.find()) {
            cleanedAmenities = matcher.group(1).trim();
        }

        // Remove any trailing periods
        if (cleanedAmenities.endsWith(".")) {
            cleanedAmenities = cleanedAmenities.substring(0, cleanedAmenities.length() - 1);
        }
        return cleanedAmenities;
    }
}
