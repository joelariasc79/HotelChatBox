package com.hotel.chatbox.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel_room_types") // Renamed table to clarify its purpose
public class HotelRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int hotelRoomTypeId; // Renamed ID to reflect its purpose as a type offering

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel; 

    @ManyToOne
    private RoomType type;

    @ManyToMany
    private Set<Amenities> amenities; 

    @Column(nullable = false)
    private int totalRoomsOfType; // Represents the total number of physical rooms of this type at the hotel
    // Renamed from 'noRooms' to clarify. 'noRooms' was at 

    private float price;
    private float discount;
    private String description;
    private String policies;

    @Transient
    private String hotelName;
    @Transient
    private String roomTypeName; // Renamed from roomType to be more descriptive. Was 

    @Transient
    private Set<String> hotelRoomAmenityNames = new HashSet<>();

    // New: One hotel room type can have many actual physical rooms
    @OneToMany(mappedBy = "hotelRoomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> physicalRooms = new HashSet<>();


    // Example of adjusted getter (from )
    public String getHotelName() {
        return hotel != null ? hotel.getHotelName() : null;
    }

    // Example of adjusted getter for room type name
    public String getRoomTypeName() {
        return type != null ? type.getName() : null;
    }

	public HotelRoom() {
		super();
	}

	public HotelRoom(int hotelRoomTypeId, Hotel hotel, RoomType type, Set<Amenities> amenities, int totalRoomsOfType,
			float price, float discount, String description, String policies, String hotelName, String roomTypeName,
			Set<String> hotelRoomAmenityNames, Set<Room> physicalRooms) {
		super();
		this.hotelRoomTypeId = hotelRoomTypeId;
		this.hotel = hotel;
		this.type = type;
		this.amenities = amenities;
		this.totalRoomsOfType = totalRoomsOfType;
		this.price = price;
		this.discount = discount;
		this.description = description;
		this.policies = policies;
		this.hotelName = hotelName;
		this.roomTypeName = roomTypeName;
		this.hotelRoomAmenityNames = hotelRoomAmenityNames;
		this.physicalRooms = physicalRooms;
	}

	public int getHotelRoomTypeId() {
		return hotelRoomTypeId;
	}

	public void setHotelRoomTypeId(int hotelRoomTypeId) {
		this.hotelRoomTypeId = hotelRoomTypeId;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public RoomType getType() {
		return type;
	}

	public void setType(RoomType type) {
		this.type = type;
	}

	public Set<Amenities> getAmenities() {
		return amenities;
	}

	public void setAmenities(Set<Amenities> amenities) {
		this.amenities = amenities;
	}

	public int getTotalRoomsOfType() {
		return totalRoomsOfType;
	}

	public void setTotalRoomsOfType(int totalRoomsOfType) {
		this.totalRoomsOfType = totalRoomsOfType;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPolicies() {
		return policies;
	}

	public void setPolicies(String policies) {
		this.policies = policies;
	}

	public Set<String> getHotelRoomAmenityNames() {
		return hotelRoomAmenityNames;
	}

	public void setHotelRoomAmenityNames(Set<String> hotelRoomAmenityNames) {
		this.hotelRoomAmenityNames = hotelRoomAmenityNames;
	}

	public Set<Room> getPhysicalRooms() {
		return physicalRooms;
	}

	public void setPhysicalRooms(Set<Room> physicalRooms) {
		this.physicalRooms = physicalRooms;
	}

	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.roomTypeName = roomTypeName;
	}
}



//package com.hotel.chatbox.domain;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name="hotel_rooms")
//public class HotelRoom {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // IMPORTANT: If hotelRoomId is auto-generated
//    private int hotelRoomId; //PK
//
//    @ManyToOne // This defines the many-to-one relationship
//    @JoinColumn(name = "hotel_id") // This specifies the foreign key column in the hotel_rooms table
//    private Hotel hotel; // Add this field to link HotelRoom to Hotel
//
//    @ManyToOne
//    private RoomType type;
//    @ManyToMany
//    private Set<Amenities> amenities;
//    private int noRooms;
//    private float price;
//    private float discount;
//    private String description;
//    private String policies;
//
//    @Transient
//    private String hotelName; // This can now be derived from 'hotel.hotelName'
//    @Transient
//    private String roomType; // This can now be derived from 'type.name' or similar
//
//    @Transient
//    private Set<String> hotelRoomAmenityNames = new HashSet<>();
//
//    // --- Getters and Setters ---
//    public Hotel getHotel() {
//        return hotel;
//    }
//
//    public void setHotel(Hotel hotel) {
//        this.hotel = hotel;
//    }
//
//    // Existing getters/setters...
//    public String getHotelName() {
//        // You might consider populating this from the 'hotel' object if it's transient
//        return hotel != null ? hotel.getHotelName() : null;
//    }
//    public void setHotelName(String hotelName) {
//        // If this is truly transient, you might not need a setter or it's for display
//        this.hotelName = hotelName;
//    }
//    public String getRoomType() {
//        return roomType;
//    }
//    public void setRoomType(String roomType) {
//        this.roomType = roomType;
//    }
//    public int getHotelRoomId() {
//        return hotelRoomId;
//    }
//    public void setHotelRoomId(int hotelRoomId) {
//        this.hotelRoomId = hotelRoomId;
//    }
//    public RoomType getType() {
//        return type;
//    }
//    public void setType(RoomType type) {
//        this.type = type;
//    }
//
//    public Set<Amenities> getAmenities() {
//        return amenities;
//    }
//    // Corrected setter name for 'amenities' field: Changed from setHotelRoomAmenities
//    public void setAmenities(Set<Amenities> amenities) {
//        this.amenities = amenities;
//    }
//    public Set<String> getHotelRoomAmenityNames() {
//        return hotelRoomAmenityNames;
//    }
//    public void setHotelRoomAmenityNames(Set<String> hotelRoomAmenityNames) {
//        this.hotelRoomAmenityNames = hotelRoomAmenityNames;
//    }
//    public int getNoRooms() {
//        return noRooms;
//    }
//    public void setNoRooms(int noRooms) {
//        this.noRooms = noRooms;
//    }
//    public float getPrice() {
//        return price;
//    }
//    public void setPrice(float price) {
//        this.price = price;
//    }
//    public float getDiscount() {
//        return discount;
//    }
//    public void setDiscount(float discount) {
//        this.discount = discount;
//    }
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String description) {
//        this.description = description;
//    }
//    public String getPolicies() {
//        return policies;
//    }
//    public void setPolicies(String policies) {
//        this.policies = policies;
//    }
//}
