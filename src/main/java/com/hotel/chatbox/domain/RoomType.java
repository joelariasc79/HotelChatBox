package com.hotel.chatbox.domain;


import jakarta.persistence.*;

@Entity
@Table(name="roomtype")
public class RoomType {
	@Id
	private int typeId;
	private String name;
	
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
