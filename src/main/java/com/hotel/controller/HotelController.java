package com.hotel.controller;

import com.hotel.dto.ReservationRequest;
import com.hotel.dto.ServiceRequest;
import com.hotel.facade.HotelFacade;
import com.hotel.model.Invoice;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Thin REST layer; all logic goes through HotelFacade.
@CrossOrigin(origins = "${FRONTEND_ORIGIN:*}")
@RestController
@RequestMapping("/api/hotel")
public class HotelController {

  private final HotelFacade facade;

  public HotelController(HotelFacade facade) {
    this.facade = facade;
  }

  @PostMapping("/reservar")
  public ResponseEntity<Reservation> reserve(@RequestBody ReservationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(facade.createReservation(req));
  }

  @GetMapping("/disponibilidad")
  public List<Room> availability(@RequestParam(required = false) String checkIn,
      @RequestParam(required = false) String checkOut,
      @RequestParam(required = false) String type) {
    return facade.findAvailable(checkIn, checkOut, type);
  }

  @PostMapping("/servicios/{reservationId}")
  public Reservation addService(@PathVariable String reservationId,
      @RequestBody ServiceRequest req) {
    return facade.addService(reservationId, req == null ? null : req.getServiceType());
  }

  @PutMapping("/checkin/{reservationId}")
  public Reservation checkIn(@PathVariable String reservationId) {
    return facade.checkIn(reservationId);
  }

  @PutMapping("/checkout/{reservationId}")
  public Invoice checkOut(@PathVariable String reservationId) {
    return facade.checkOut(reservationId);
  }

  @GetMapping("/reserva/{reservationId}")
  public Reservation getReservation(@PathVariable String reservationId) {
    return facade.getReservation(reservationId);
  }

  @GetMapping("/habitaciones")
  public List<Room> allRooms() {
    return facade.findAllRooms();
  }
}
