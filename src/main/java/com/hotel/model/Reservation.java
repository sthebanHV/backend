package com.hotel.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {

  @Id
  private String id;

  private String guestName;
  private String guestEmail;
  private int roomNumber;

  @Enumerated(EnumType.STRING)
  private RoomType roomType;

  private LocalDate checkIn;
  private LocalDate checkOut;
  private int nights;

  @Enumerated(EnumType.STRING)
  private ReservationStatus status;

  private double roomTotal;

  @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL,
      orphanRemoval = true, fetch = FetchType.EAGER)
  private List<ExtraItem> extras = new ArrayList<>();

  private double extrasTotal;
  private String accessKey;
  private LocalDateTime createdAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String guestName) {
    this.guestName = guestName;
  }

  public String getGuestEmail() {
    return guestEmail;
  }

  public void setGuestEmail(String guestEmail) {
    this.guestEmail = guestEmail;
  }

  public int getRoomNumber() {
    return roomNumber;
  }

  public void setRoomNumber(int roomNumber) {
    this.roomNumber = roomNumber;
  }

  public RoomType getRoomType() {
    return roomType;
  }

  public void setRoomType(RoomType roomType) {
    this.roomType = roomType;
  }

  public LocalDate getCheckIn() {
    return checkIn;
  }

  public void setCheckIn(LocalDate checkIn) {
    this.checkIn = checkIn;
  }

  public LocalDate getCheckOut() {
    return checkOut;
  }

  public void setCheckOut(LocalDate checkOut) {
    this.checkOut = checkOut;
  }

  public int getNights() {
    return nights;
  }

  public void setNights(int nights) {
    this.nights = nights;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus status) {
    this.status = status;
  }

  public double getRoomTotal() {
    return roomTotal;
  }

  public void setRoomTotal(double roomTotal) {
    this.roomTotal = roomTotal;
  }

  public List<ExtraItem> getExtras() {
    return extras;
  }

  public void setExtras(List<ExtraItem> extras) {
    this.extras = extras;
  }

  public double getExtrasTotal() {
    return extrasTotal;
  }

  public void setExtrasTotal(double extrasTotal) {
    this.extrasTotal = extrasTotal;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
