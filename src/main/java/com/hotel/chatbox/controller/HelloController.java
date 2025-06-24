package com.hotel.chatbox.controller;

import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize; // Optional, for role-based access
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    // This endpoint is protected and requires a valid JWT
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from a secured endpoint!");
    }

//    @GetMapping("/admin/hello")
    // Example of role-based access: requires authenticated user with 'ADMIN' role
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> adminHello() {
//        return ResponseEntity.ok("Hello Admin! This is an admin-only endpoint.");
//    }
}