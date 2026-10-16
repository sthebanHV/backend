package com.hotel.repository;

import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
  List<Reservation> findByRoomNumberAndStatusNotAndCheckInBeforeAndCheckOutAfter(
      int roomNumber, ReservationStatus status, LocalDate checkOut, LocalDate checkIn);
}
