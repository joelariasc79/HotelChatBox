package com.hotel.chatbox.model;

//import lombok.Getter;
//import lombok.Setter;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;

// DTO for authentication request (username, password)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
    
    
    public AuthRequest() {
		super();
	}
    
	public AuthRequest(String username, String password) {
		super();
		this.username = username;
		this.password = password;
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
	 
}

