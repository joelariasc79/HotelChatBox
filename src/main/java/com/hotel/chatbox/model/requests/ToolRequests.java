package com.hotel.chatbox.model.requests;

public class ToolRequests {

//    public record RoomBookingRequest(String roomType, String checkInDate, String checkOutDate, int numberOfGuests) {}
    public record RoomServiceRequest(String time, String items, String room_id, String stay_id) {}
    public record HousekeepingRequest(String date, String time, String room_id) {}
    public record SpaReservationRequest(String date, String time, String service) {}
    public record GymReservationRequest(String date, String time) {}
    public record CheckInRequest(Long bookingId) {}

}