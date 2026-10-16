package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Invoice {
  private String invoiceNumber;
  private String reservationId;
  private String guestName;
  private int roomNumber;
  private RoomType roomType;
  private LocalDate checkIn;
  private LocalDate checkOut;
  private int nights;
  private List<InvoiceLine> lines;
  private double roomTotal;
  private double extrasTotal;
  private double total;
  private LocalDateTime issuedAt;

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public void setInvoiceNumber(String invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  public String getReservationId() {
    return reservationId;
  }

  public void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String guestName) {
    this.guestName = guestName;
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

  public List<InvoiceLine> getLines() {
    return lines;
  }

  public void setLines(List<InvoiceLine> lines) {
    this.lines = lines;
  }

  public double getRoomTotal() {
    return roomTotal;
  }

  public void setRoomTotal(double roomTotal) {
    this.roomTotal = roomTotal;
  }

  public double getExtrasTotal() {
    return extrasTotal;
  }

  public void setExtrasTotal(double extrasTotal) {
    this.extrasTotal = extrasTotal;
  }

  public double getTotal() {
    return total;
  }

  public void setTotal(double total) {
    this.total = total;
  }

  public LocalDateTime getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(LocalDateTime issuedAt) {
    this.issuedAt = issuedAt;
  }
}
