package com.hotel.chatbox.dto;

import java.util.List;

import com.hotel.chatbox.parser.HotelParser.HotelInfo;;


//DTO to combine chat message and hotel list
public class ChatResponse {
    private String response; // The chat message (Mono<String> equivalent)
    private List<HotelInfo> hotels; // The hotel list (Object equivalent)

    public ChatResponse(String response, List<HotelInfo> hotels) {
        this.response = response;
        this.hotels = hotels;
    }

    // Getters
    public String getResponse() {
        return response;
    }

    public List<HotelInfo> getHotels() {
        return hotels;
    }

    // You might want a constructor for just a message too
    public ChatResponse(String response) {
        this.response = response;
        this.hotels = null; // No hotels in this case
    }
}