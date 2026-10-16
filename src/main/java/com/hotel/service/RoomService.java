package com.hotel.service;

import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

// Tracks the 15 rooms and checks date overlaps against stored reservations.
@Service
public class RoomService {

  private final RoomRepository rooms;
  private final ReservationRepository reservations;

  public RoomService(RoomRepository rooms, ReservationRepository reservations) {
    this.rooms = rooms;
    this.reservations = reservations;
  }

  @PostConstruct
  public void seedRooms() {
    if (rooms.count() > 0) {
      return;
    }
    for (int n = 101; n <= 106; n++) {
      rooms.save(new Room(n, RoomType.SINGLE, 80));
    }
    for (int n = 201; n <= 206; n++) {
      rooms.save(new Room(n, RoomType.DOUBLE, 120));
    }
    for (int n = 301; n <= 303; n++) {
      rooms.save(new Room(n, RoomType.SUITE, 250));
    }
  }

  public List<Room> findAll() {
    return rooms.findAll().stream()
        .sorted(Comparator.comparingInt(Room::getNumber))
        .toList();
  }

  public Optional<Room> findByNumber(int number) {
    return rooms.findById(number);
  }

  public boolean isAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
    return reservations.findByRoomNumberAndStatusNotAndCheckInBeforeAndCheckOutAfter(
        roomNumber, ReservationStatus.CHECKED_OUT, checkOut, checkIn).isEmpty();
  }

  public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut, RoomType type) {
    List<Room> candidates = type == null
        ? rooms.findAll()
        : rooms.findByTypeOrderByNumberAsc(type);
    return candidates.stream()
        .filter(r -> isAvailable(r.getNumber(), checkIn, checkOut))
        .sorted(Comparator.comparingInt(Room::getNumber))
        .toList();
  }
}
