package com.hotel.chatbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_room_id", nullable = false) // This now refers to the room *type* booked
    private HotelRoom hotelRoom;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private int numberOfRoomsBooked;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = true)
    private String couponCodeUsed;

    // New: One booking can have multiple actual stays (e.g., if multiple rooms are booked, or if tracking extensions)
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Stay> stays = new HashSet<>();

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }

	public Booking() {
		super();
	}

	public Booking(Long bookingId, User user, HotelRoom hotelRoom, LocalDate checkInDate, LocalDate checkOutDate,
			int numberOfRoomsBooked, int numberOfGuests, double totalPrice, BookingStatus status, String couponCodeUsed,
			Set<Stay> stays) {
		super();
		this.bookingId = bookingId;
		this.user = user;
		this.hotelRoom = hotelRoom;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.numberOfRoomsBooked = numberOfRoomsBooked;
		this.numberOfGuests = numberOfGuests;
		this.totalPrice = totalPrice;
		this.status = status;
		this.couponCodeUsed = couponCodeUsed;
		this.stays = stays;
	}

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public HotelRoom getHotelRoom() {
		return hotelRoom;
	}

	public void setHotelRoom(HotelRoom hotelRoom) {
		this.hotelRoom = hotelRoom;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public int getNumberOfRoomsBooked() {
		return numberOfRoomsBooked;
	}

	public void setNumberOfRoomsBooked(int numberOfRoomsBooked) {
		this.numberOfRoomsBooked = numberOfRoomsBooked;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public void setNumberOfGuests(int numberOfGuests) {
		this.numberOfGuests = numberOfGuests;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public String getCouponCodeUsed() {
		return couponCodeUsed;
	}

	public void setCouponCodeUsed(String couponCodeUsed) {
		this.couponCodeUsed = couponCodeUsed;
	}

	public Set<Stay> getStays() {
		return stays;
	}

	public void setStays(Set<Stay> stays) {
		this.stays = stays;
	}

}
