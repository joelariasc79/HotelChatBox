package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UserLoyalty loyalty;

     @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
    private UserPreference preferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Feedback> feedbackSubmissions = new HashSet<>();

    @Column(nullable = true)
    private LocalDate lastStayDate; // For personalized offers based on recent activity

    @Column(nullable = false)
    private int totalBookingsCount = 0; // For personalized offers based on booking history

    public User() {
		super();
	}
    
	public User(Long id, String username, String password, String role, UserLoyalty loyalty, UserPreference preferences,
			Set<Booking> bookings, Set<Feedback> feedbackSubmissions, LocalDate lastStayDate, int totalBookingsCount) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.loyalty = loyalty;
		this.preferences = preferences;
		this.bookings = bookings;
		this.feedbackSubmissions = feedbackSubmissions;
		this.lastStayDate = lastStayDate;
		this.totalBookingsCount = totalBookingsCount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public UserLoyalty getLoyalty() {
		return loyalty;
	}

	public void setLoyalty(UserLoyalty loyalty) {
		this.loyalty = loyalty;
	}

	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}

	public Set<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(Set<Booking> bookings) {
		this.bookings = bookings;
	}

	public Set<Feedback> getFeedbackSubmissions() {
		return feedbackSubmissions;
	}

	public void setFeedbackSubmissions(Set<Feedback> feedbackSubmissions) {
		this.feedbackSubmissions = feedbackSubmissions;
	}

	public LocalDate getLastStayDate() {
		return lastStayDate;
	}

	public void setLastStayDate(LocalDate lastStayDate) {
		this.lastStayDate = lastStayDate;
	}

	public int getTotalBookingsCount() {
		return totalBookingsCount;
	}

	public void setTotalBookingsCount(int totalBookingsCount) {
		this.totalBookingsCount = totalBookingsCount;
	}

   
}



//package com.hotel.chatbox.domain;
//
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
////import lombok.Getter;
////import lombok.Setter;
////import lombok.NoArgsConstructor;
////import lombok.AllArgsConstructor;
//
//@Entity
//@Table(name = "users") // Renamed from 'user' to 'users' to avoid potential SQL keyword conflicts
////@Getter
////@Setter
////@NoArgsConstructor
////@AllArgsConstructor
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String username;
//	private String password; // Storing hashed password
//    private String role; // e.g., "USER", "ADMIN"
//    
//   
//    public User() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//    
//	public User(Long id, String username, String password, String role) {
//		super();
//		this.id = id;
//		this.username = username;
//		this.password = password;
//		this.role = role;
//	}
//	public Long getId() {
//		return id;
//	}
//	public void setId(Long id) {
//		this.id = id;
//	}
//	public String getUsername() {
//		return username;
//	}
//	public void setUsername(String username) {
//		this.username = username;
//	}
//	public String getPassword() {
//		return password;
//	}
//	public void setPassword(String password) {
//		this.password = password;
//	}
//	public String getRole() {
//		return role;
//	}
//	public void setRole(String role) {
//		this.role = role;
//	}
//}
