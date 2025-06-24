package com.hotel.chatbox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // New: UserPreference will have its own auto-generated ID
    private Long id; // This is the independent primary key for UserPreference

    @OneToOne(fetch = FetchType.LAZY)
    // No @MapsId anymore. This is a regular foreign key relationship.
    @JoinColumn(name = "user_id", unique = true, nullable = false) // FK to User table. 'unique=true' for 1:1. 'nullable=false' if a preference must always have a user.
    private User user;

    @Version // Keep this! It successfully addressed the previous optimistic locking issue.
    private Long version;

    @Column(nullable = true)
    private String preferredRoomType;

    @Column(nullable = true)
    private String dietaryRestrictions;

    @Column(nullable = true)
    private String preferredAmenities;

    @Column(nullable = true)
    private String communicationPreference;

    // --- Constructors ---
    public UserPreference() {
    }

    public UserPreference(User user) {
        this.user = user;
        // REMOVED: this.userId = user.getId(); // No longer needed as 'id' is auto-generated
    }

    // --- Getters and Setters ---
    public Long getId() { // New getter for the new primary key 'id'
        return id;
    }

    public void setId(Long id) { // New setter for the new primary key 'id'
        this.id = id;
    }

    // REMOVED: getUserId() and setUserId() as 'userId' is no longer the primary key field

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getPreferredAmenities() {
        return preferredAmenities;
    }

    public void setPreferredAmenities(String preferredAmenities) {
        this.preferredAmenities = preferredAmenities;
    }

    public String getCommunicationPreference() {
        return communicationPreference;
    }

    public void setCommunicationPreference(String communicationPreference) {
        this.communicationPreference = communicationPreference;
    }
}

//package com.hotel.chatbox.domain;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "user_preferences")
//public class UserPreference {
//    @Id
//    private Long userId; // PK is also FK to User table
//
//    @OneToOne
//    @MapsId // Maps the primary key of this entity to the primary key of the associated entity
//    @JoinColumn(name = "id") // Or "user_id" if that's the actual column name in your DB
//    private User user;
//
//    @Version // <--- ADD THIS LINE
//    private Long version; // <--- ADD THIS LINE (and its getter/setter)
//
//    @Column(nullable = true)
//    private String preferredRoomType;
//
//    @Column(nullable = true)
//    private String dietaryRestrictions;
//
//    @Column(nullable = true)
//    private String preferredAmenities;
//
//    @Column(nullable = true)
//    private String communicationPreference;
//
//    // --- Constructors ---
//    public UserPreference() {
//    }
//
//    public UserPreference(User user) {
//        this.user = user;
//        this.userId = user.getId();
//    }
//
//    // --- Getters and Setters ---
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    // Add getter and setter for version
//    public Long getVersion() {
//        return version;
//    }
//
//    public void setVersion(Long version) {
//        this.version = version;
//    }
//
//    public String getPreferredRoomType() {
//        return preferredRoomType;
//    }
//
//    public void setPreferredRoomType(String preferredRoomType) {
//        this.preferredRoomType = preferredRoomType;
//    }
//
//    public String getDietaryRestrictions() {
//        return dietaryRestrictions;
//    }
//
//    public void setDietaryRestrictions(String dietaryRestrictions) {
//        this.dietaryRestrictions = dietaryRestrictions;
//    }
//
//    public String getPreferredAmenities() {
//        return preferredAmenities;
//    }
//
//    public void setPreferredAmenities(String preferredAmenities) {
//        this.preferredAmenities = preferredAmenities;
//    }
//
//    public String getCommunicationPreference() {
//        return communicationPreference;
//    }
//
//    public void setCommunicationPreference(String communicationPreference) {
//        this.communicationPreference = communicationPreference;
//    }
//}
//
//
////package com.hotel.chatbox.domain;
////
////import jakarta.persistence.*;
////
////@Entity
////@Table(name = "user_preferences")
////public class UserPreference {
////    @Id
////    private Long userId; // PK is also FK to User table
////
////    @OneToOne
////    @MapsId // Maps the primary key of this entity to the primary key of the associated entity
////    @JoinColumn(name = "id") // Name of the foreign key column
////    private User user;
////
////    @Column(nullable = true)
////    private String preferredRoomType; // e.g., "DELUXE", "SUITE"
////
////    @Column(nullable = true)
////    private String dietaryRestrictions; // e.g., "Vegetarian", "Gluten-Free"
////
////    @Column(nullable = true)
////    private String preferredAmenities; // e.g., "Pool, Gym" (can be comma-separated or JSON)
////
////    @Column(nullable = true)
////    private String communicationPreference; // e.g., "EMAIL", "SMS", "NONE"
////
////    // --- Constructors ---
////    public UserPreference() {
////    }
////
////    public UserPreference(User user) {
////        this.user = user;
////        this.userId = user.getId();
////    }
////
////    // --- Getters and Setters ---
////    public Long getUserId() {
////        return userId;
////    }
////
////    public void setUserId(Long userId) {
////        this.userId = userId;
////    }
////
////    public User getUser() {
////        return user;
////    }
////
////    public void setUser(User user) {
////        this.user = user;
////    }
////
////    public String getPreferredRoomType() {
////        return preferredRoomType;
////    }
////
////    public void setPreferredRoomType(String preferredRoomType) {
////        this.preferredRoomType = preferredRoomType;
////    }
////
////    public String getDietaryRestrictions() {
////        return dietaryRestrictions;
////    }
////
////    public void setDietaryRestrictions(String dietaryRestrictions) {
////        this.dietaryRestrictions = dietaryRestrictions;
////    }
////
////    public String getPreferredAmenities() {
////        return preferredAmenities;
////    }
////
////    public void setPreferredAmenities(String preferredAmenities) {
////        this.preferredAmenities = preferredAmenities;
////    }
////
////    public String getCommunicationPreference() {
////        return communicationPreference;
////    }
////
////    public void setCommunicationPreference(String communicationPreference) {
////        this.communicationPreference = communicationPreference;
////    }
////}