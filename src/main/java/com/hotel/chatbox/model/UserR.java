package com.hotel.chatbox.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;

@Entity
@Table(name = "users") // Renamed from 'user' to 'users' to avoid potential SQL keyword conflicts
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
public class UserR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password; // Storing hashed password
    private String role; // e.g., "USER", "ADMIN"
}
