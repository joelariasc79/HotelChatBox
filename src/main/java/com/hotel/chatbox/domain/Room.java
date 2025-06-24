package com.hotel.chatbox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_room_type_id", nullable = false) // Links to the specific room type offered by the hotel
    private HotelRoom hotelRoomType; // Renamed HotelRoom to represent the type offering

    // @Column(nullable = false, unique = true)
    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomAvailabilityStatus availabilityStatus;

    @Column(nullable = true)
    private Integer currentOccupancy; // Can be null if room is vacant

    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes; // e.g., "Adjacent to elevator", "Recently renovated"

    public enum RoomAvailabilityStatus {
        AVAILABLE, OCCUPIED, UNDER_MAINTENANCE, CLEANING, RESERVED
    }

	public Room() {
		super();
	}

	public Room(Long roomId, Hotel hotel, HotelRoom hotelRoomType, String roomNumber,
			RoomAvailabilityStatus availabilityStatus, Integer currentOccupancy, String notes) {
		super();
		this.roomId = roomId;
		this.hotel = hotel;
		this.hotelRoomType = hotelRoomType;
		this.roomNumber = roomNumber;
		this.availabilityStatus = availabilityStatus;
		this.currentOccupancy = currentOccupancy;
		this.notes = notes;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public HotelRoom getHotelRoomType() {
		return hotelRoomType;
	}

	public void setHotelRoomType(HotelRoom hotelRoomType) {
		this.hotelRoomType = hotelRoomType;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public RoomAvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(RoomAvailabilityStatus availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public Integer getCurrentOccupancy() {
		return currentOccupancy;
	}

	public void setCurrentOccupancy(Integer currentOccupancy) {
		this.currentOccupancy = currentOccupancy;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}   
}