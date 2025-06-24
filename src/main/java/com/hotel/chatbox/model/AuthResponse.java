package com.hotel.chatbox.model;

//import lombok.Getter;
//import lombok.Setter;
//import lombok.AllArgsConstructor;

// DTO for authentication response (JWT token)
//@Getter
//@Setter
//@AllArgsConstructor
public class AuthResponse {
 private final String jwtToken; // This name 'jwtToken' is critical!

 public AuthResponse(String jwtToken) {
     this.jwtToken = jwtToken;
 }

 public String getJwtToken() { // This getter is critical for Jackson to serialize
     return jwtToken;
 }
}


