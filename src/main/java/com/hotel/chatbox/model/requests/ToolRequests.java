package com.hotel.chatbox.model.requests;

public class ToolRequests {

//    public record RoomBookingRequest(String roomType, String checkInDate, String checkOutDate, int numberOfGuests) {}
//    public record RoomServiceRequest(String time, String items, Long room_id, Long stay_id) {}
//    public record RoomServiceRequest(String time, String items, Long room_id, Long stay_id) {}
    public record HousekeepingRequest(String time, Long room_id, Long stay_id) {}
    public record SpaReservationRequest(String date, String time, String service, Long stay_id) {}
    public record GymReservationRequest(String date, String time, Long stay_id) {}
    public record CheckInRequest(Long bookingId) {}

}