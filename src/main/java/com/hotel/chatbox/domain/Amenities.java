package com.hotel.chatbox.domain;

import jakarta.persistence.*;

@Entity
@Table(name="amenities")
public class Amenities {
	@Id
	private int a_id;
	private String name;
	
	public int getA_id() {
		return a_id;
	}
	public void setA_id(int a_id) {
		this.a_id = a_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
