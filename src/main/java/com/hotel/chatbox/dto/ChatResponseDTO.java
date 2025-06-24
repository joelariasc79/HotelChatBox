// src/main/java/com/hotel/chatbox/dto/ChatResponseDTO.java
package com.hotel.chatbox.dto;

import com.hotel.chatbox.parser.HotelParser; // Import your HotelParser
import java.util.List;

public class ChatResponseDTO {
    private String chatMessage;
    private List<HotelParser.HotelInfo> hotelList;

    // Default constructor (required for JSON deserialization)
    public ChatResponseDTO() {
    }

    public ChatResponseDTO(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatResponseDTO(String chatMessage, List<HotelParser.HotelInfo> hotelList) {
        this.chatMessage = chatMessage;
        this.hotelList = hotelList;
    }

    // Getters and Setters
    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public List<HotelParser.HotelInfo> getHotelList() {
        return hotelList;
    }

    public void setHotelList(List<HotelParser.HotelInfo> hotelList) {
        this.hotelList = hotelList;
    }
}