package com.hotel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room {

  @Id
  private int number;

  @Enumerated(EnumType.STRING)
  private RoomType type;

  private double basePrice;

  public Room() {
  }

  public Room(int number, RoomType type, double basePrice) {
    this.number = number;
    this.type = type;
    this.basePrice = basePrice;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public RoomType getType() {
    return type;
  }

  public void setType(RoomType type) {
    this.type = type;
  }

  public double getBasePrice() {
    return basePrice;
  }

  public void setBasePrice(double basePrice) {
    this.basePrice = basePrice;
  }
}
