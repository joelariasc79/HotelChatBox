package com.hotel.chatbox.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookingResponseParser {

    // Record to hold the extracted booking details
    public record BookingDetails(
            String roomType,
            String hotelName,
            Integer numberOfGuests,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {}

    // Define all expected date formatters in an array for iterative parsing
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH), // Corrected 'BBBB' to 'yyyy'
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };
    
    public boolean canBeParsed(String response) {
    	boolean out = response.trim().contains("book");
        return out;
    }

    private LocalDate parseAnyDateFormat(String dateString) {
        if (dateString == null) {
            return null;
        }
        // Try each formatter until one successfully parses the date string
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateString.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Continue to the next formatter if parsing fails with the current one
            }
        }
        System.err.println("Could not parse date string with any known format: '" + dateString + "'");
        return null;
    }
    
    public BookingDetails parseBookingConfirmation(String response) {
        // Preliminary check to see if the response is even a booking confirmation
        if (!canBeParsed(response)) {
            System.out.println("DEBUG: Response does not start with expected booking prefix. Skipping detailed parsing.");
            return null;
        }

        String roomType = null;
        String hotelName = null;
        Integer numberOfGuests = null;
        LocalDate checkInDate = null;
        LocalDate checkOutDate = null;

        // Regex for RoomType (Standard, Deluxe, Suite) - "a [RoomType] room"
        Pattern roomTypePattern = Pattern.compile("a\\s+(Standard|Deluxe|Suite)\\s+room", Pattern.CASE_INSENSITIVE);
        Matcher roomTypeMatcher = roomTypePattern.matcher(response);
        if (roomTypeMatcher.find()) {
            roomType = roomTypeMatcher.group(1);
        }

        // Regex for Hotel Name - "(at|in) (the)? [Hotel Name (ending with Hotel|Resort|Inn|Motel|Lodge)]"
        Pattern hotelNamePattern = Pattern.compile("(?:at|in)\\s+(?:the\\s+)?([\\w\\s,.'-]+(?:Hotel|Resort|Inn|Motel|Lodge))", Pattern.CASE_INSENSITIVE);
        Matcher hotelNameMatcher = hotelNamePattern.matcher(response);
        if (hotelNameMatcher.find()) {
            hotelName = hotelNameMatcher.group(1).trim();
            // Clean up common endings if they are accidentally duplicated (e.g., "Resort Resort")
            hotelName = hotelName.replaceAll("(Hotel|Resort|Inn|Motel|Lodge)\\s*(Hotel|Resort|Inn|Motel|Lodge)$", "$1");
        }

        // Regex for Guests Number - "for [X] guests"
        Pattern guestsPattern = Pattern.compile("for\\s+(\\d+)\\s+guests", Pattern.CASE_INSENSITIVE);
        Matcher guestsMatcher = guestsPattern.matcher(response);
        if (guestsMatcher.find()) {
            try {
                numberOfGuests = Integer.parseInt(guestsMatcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("Could not parse number of guests from: " + guestsMatcher.group(1));
            }
        }

        // --- Consolidated Date Parsing Logic ---
        // Regex to capture a date string in either "Month Day, Year" or "MM/DD/YYYY" format
        // This pattern needs to be broad enough to capture both date formats correctly.
        // It's crucial that the date format is captured fully and then parsed.
        String dateStringCaptureRegex = "(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{1,2},\\s+\\d{4}|\\d{2}/\\d{2}/\\d{4}";

        // 1. Try pattern: "from [DATE_STRING] to [DATE_STRING]"
        Pattern fromToDatePattern = Pattern.compile("from\\s+("+dateStringCaptureRegex+")\\s+to\\s+("+dateStringCaptureRegex+")", Pattern.CASE_INSENSITIVE);
        Matcher fromToDateMatcher = fromToDatePattern.matcher(response);

        // 2. Try pattern: "check-in date of [DATE_STRING] and a check-out date of [DATE_STRING]"
        Pattern checkInOutDatePattern = Pattern.compile("check-in date of\\s+("+dateStringCaptureRegex+")\\s+and a check-out date of\\s+("+dateStringCaptureRegex+")", Pattern.CASE_INSENSITIVE);
        Matcher checkInOutDateMatcher = checkInOutDatePattern.matcher(response);
        
        // 3. Try pattern: "from [MM/DD/YYYY] to [MM/DD/YYYY]" in the "successfully booked" part
        Pattern bookedFromToDatePattern = Pattern.compile("booked for \\d+ guests from\\s+("+dateStringCaptureRegex+")\\s+to\\s+("+dateStringCaptureRegex+")", Pattern.CASE_INSENSITIVE);
        Matcher bookedFromToDateMatcher = bookedFromToDatePattern.matcher(response);


        if (fromToDateMatcher.find()) {
            checkInDate = parseAnyDateFormat(fromToDateMatcher.group(1));
            checkOutDate = parseAnyDateFormat(fromToDateMatcher.group(2));
        } else if (checkInOutDateMatcher.find()) {
            checkInDate = parseAnyDateFormat(checkInOutDateMatcher.group(1));
            checkOutDate = parseAnyDateFormat(checkInOutDateMatcher.group(2));
        } else if (bookedFromToDateMatcher.find()) {
            checkInDate = parseAnyDateFormat(bookedFromToDateMatcher.group(1));
            checkOutDate = parseAnyDateFormat(bookedFromToDateMatcher.group(2));
        }

        // --- End Consolidated Date Parsing Logic ---

        // Return BookingDetails if all critical information is found
        if (roomType != null && hotelName != null && numberOfGuests != null && checkInDate != null && checkOutDate != null) {
            return new BookingDetails(roomType, hotelName, numberOfGuests, checkInDate, checkOutDate);
        } else {
            // Log specific missing fields for better error diagnosis
            System.err.println("ERROR: Incomplete booking details parsed. Missing info for: " +
                               (roomType == null ? "RoomType " : "") +
                               (hotelName == null ? "HotelName " : "") +
                               (numberOfGuests == null ? "Guests " : "") +
                               (checkInDate == null ? "CheckInDate " : "") +
                               (checkOutDate == null ? "CheckOutDate " : ""));
            return null;
        }
    }

//    public BookingDetails parseBookingConfirmation(String response) {
//        // Preliminary check to see if the response is even a booking confirmation
//        if (!canBeParsed(response)) {
//            System.out.println("DEBUG: Response does not start with expected booking prefix. Skipping detailed parsing.");
//            return null;
//        }
//
//        String roomType = null;
//        String hotelName = null;
//        Integer numberOfGuests = null;
//        LocalDate checkInDate = null;
//        LocalDate checkOutDate = null;
//        
//        // System.out.println("**************************************: ");
//
//        // Regex for RoomType (Standard, Deluxe, Suite) - "a [RoomType] room"
//        Pattern roomTypePattern = Pattern.compile("a\\s+(Standard|Deluxe|Suite)\\s+room", Pattern.CASE_INSENSITIVE);
//        Matcher roomTypeMatcher = roomTypePattern.matcher(response);
//        if (roomTypeMatcher.find()) {
//            roomType = roomTypeMatcher.group(1);
//        }
//        
//        // System.out.println("**************************************: ");
//
//        // Regex for Hotel Name - "(at|in) (the)? [Hotel Name (ending with Hotel|Resort|Inn|Motel|Lodge)]"
//        Pattern hotelNamePattern = Pattern.compile("(?:at|in)\\s+(?:the\\s+)?([\\w\\s,.'-]+(?:Hotel|Resort|Inn|Motel|Lodge))", Pattern.CASE_INSENSITIVE);
//        Matcher hotelNameMatcher = hotelNamePattern.matcher(response);
//        if (hotelNameMatcher.find()) {
//            hotelName = hotelNameMatcher.group(1).trim();
//            // Clean up common endings if they are accidentally duplicated (e.g., "Resort Resort")
//            hotelName = hotelName.replaceAll("(Hotel|Resort|Inn|Motel|Lodge)\\s*(Hotel|Resort|Inn|Motel|Lodge)$", "$1");
//        }
//        
//        // System.out.println("**************************************: ");
//
//        // Regex for Guests Number - "for [X] guests"
//        Pattern guestsPattern = Pattern.compile("for\\s+(\\d+)\\s+guests", Pattern.CASE_INSENSITIVE);
//        Matcher guestsMatcher = guestsPattern.matcher(response);
//        if (guestsMatcher.find()) {
//            try {
//                numberOfGuests = Integer.parseInt(guestsMatcher.group(1));
//            } catch (NumberFormatException e) {
//                System.err.println("Could not parse number of guests from: " + guestsMatcher.group(1));
//            }
//        }
//    
//        // System.out.println("**************************************: ");
//        // --- Consolidated Date Parsing Logic ---
//        
//        // A. Try pattern: "from [DATE_STRING] to [DATE_STRING]"
//        Pattern pattern = Pattern.compile("\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{1,2},\\s+\\d{4}\\b");
//        Matcher matcher = pattern.matcher(response);
//        
//        // B. Try pattern: "from [DATE_STRING] to [DATE_STRING]"
//        // Regex to capture a date string in either "Month Day, Year" or "MM/DD/YYYY" format
//        String dateStringCaptureRegex = "([A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}|\\d{2}/\\d{2}/\\d{4})";
//
//        // 1. Try pattern: "from [DATE_STRING] to [DATE_STRING]"
//        Pattern fromToDatePattern = Pattern.compile("from\\s+"+dateStringCaptureRegex+"\\s+to\\s+"+dateStringCaptureRegex, Pattern.CASE_INSENSITIVE);
//        Matcher fromToDateMatcher = fromToDatePattern.matcher(response);
//
//        // 2. Try pattern: "check-in date of [DATE_STRING] and a check-out date of [DATE_STRING]"
//        Pattern checkInOutDatePattern = Pattern.compile("check-in date of\\s+"+dateStringCaptureRegex+"\\s+and a check-out date of\\s+"+dateStringCaptureRegex, Pattern.CASE_INSENSITIVE);
//        Matcher checkInOutDateMatcher = checkInOutDatePattern.matcher(response);
//        
//        System.out.println("Dates Section: ");
//        if (matcher.find()) {
//        	System.out.println("matcher.find(): (1)");
//        	checkInDate = parseAnyDateFormat(matcher.group());
//            matcher.find();
//            System.out.println("Date found: " + matcher.group());
//            checkOutDate = parseAnyDateFormat(matcher.group());
//        }
//        
//        System.out.println("fromToDateMatcher: " + fromToDateMatcher.find());
//        System.out.println("checkInOutDateMatcher: " + checkInOutDateMatcher.find());
//        if (fromToDateMatcher.find()) {            
//            checkInDate = parseAnyDateFormat(fromToDateMatcher.group(0));
//            fromToDateMatcher.find();
//            checkOutDate = parseAnyDateFormat(fromToDateMatcher.group(0));
//        } else if (checkInOutDateMatcher.find()) {
//            checkInDate = parseAnyDateFormat(checkInOutDateMatcher.group());
//            checkInOutDateMatcher.find();
//            checkOutDate = parseAnyDateFormat(checkInOutDateMatcher.group());
//        }
//        
//        // System.out.println("**************************************: ");
//        
//        
//        // --- End Consolidated Date Parsing Logic ---
//
//        // Debugging output to see what was parsed
////        System.out.println("parser: ");
////        System.out.println("roomType: " + roomType);
////        System.out.println("hotelName: " + hotelName);
////        System.out.println("numberOfGuests: " + numberOfGuests);
////        System.out.println("checkInDate: " + checkInDate);
////        System.out.println("checkOutDate: " + checkOutDate);
//
//        // Return BookingDetails if all critical information is found
//        if (roomType != null && hotelName != null && numberOfGuests != null && checkInDate != null && checkOutDate != null) {
//            return new BookingDetails(roomType, hotelName, numberOfGuests, checkInDate, checkOutDate);
//        } else {
//            // Log specific missing fields for better error diagnosis
//            System.err.println("ERROR: Incomplete booking details parsed. Missing info for: " +
//                               (roomType == null ? "RoomType " : "") +
//                               (hotelName == null ? "HotelName " : "") +
//                               (numberOfGuests == null ? "Guests " : "") +
//                               (checkInDate == null ? "CheckInDate " : "") +
//                               (checkOutDate == null ? "CheckOutDate " : ""));
//            return null;
//        }
//    }
}