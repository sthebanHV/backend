package com.hotel.facade;

import com.hotel.exception.BadRequestException;
import com.hotel.exception.ConflictException;
import com.hotel.exception.NotFoundException;
import com.hotel.dto.ReservationRequest;
import com.hotel.model.Invoice;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.model.ServiceType;
import com.hotel.repository.ReservationRepository;
import com.hotel.service.AccessService;
import com.hotel.service.AdditionalServiceService;
import com.hotel.service.BillingService;
import com.hotel.service.RateService;
import com.hotel.service.RoomService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Single entry point for the controller; coordinates the 5 subsystems.
@Service
public class HotelFacade {

  private final RoomService roomService;
  private final RateService rateService;
  private final AdditionalServiceService additionalServiceService;
  private final BillingService billingService;
  private final AccessService accessService;
  private final ReservationRepository reservations;

  private final AtomicLong seq = new AtomicLong(1000);

  public HotelFacade(RoomService roomService, RateService rateService,
      AdditionalServiceService additionalServiceService, BillingService billingService,
      AccessService accessService, ReservationRepository reservations) {
    this.roomService = roomService;
    this.rateService = rateService;
    this.additionalServiceService = additionalServiceService;
    this.billingService = billingService;
    this.accessService = accessService;
    this.reservations = reservations;
  }

  @PostConstruct
  public void initSequence() {
    long max = reservations.findAll().stream()
        .map(Reservation::getId)
        .filter(id -> id != null && id.startsWith("R-"))
        .mapToLong(id -> {
          try {
            return Long.parseLong(id.substring(2));
          } catch (NumberFormatException e) {
            return 1000L;
          }
        }).max().orElse(1000L);
    seq.set(max);
  }

  @Transactional
  public synchronized Reservation createReservation(ReservationRequest req) {
    if (req.getGuestName() == null || req.getGuestName().isBlank()) {
      throw new BadRequestException("guestName is required");
    }
    if (req.getGuestEmail() == null || req.getGuestEmail().isBlank()) {
      throw new BadRequestException("guestEmail is required");
    }
    RoomType type = parseRoomType(req.getRoomType());
    LocalDate checkIn = parseDate(req.getCheckIn(), "checkIn");
    LocalDate checkOut = parseDate(req.getCheckOut(), "checkOut");
    if (!checkOut.isAfter(checkIn)) {
      throw new BadRequestException("checkOut must be after checkIn");
    }
    Room room = pickRoom(type, checkIn, checkOut, req.getRoomNumber());
    Reservation r = new Reservation();
    r.setId("R-" + seq.incrementAndGet());
    r.setGuestName(req.getGuestName().trim());
    r.setGuestEmail(req.getGuestEmail().trim());
    r.setRoomNumber(room.getNumber());
    r.setRoomType(room.getType());
    r.setCheckIn(checkIn);
    r.setCheckOut(checkOut);
    r.setNights((int) ChronoUnit.DAYS.between(checkIn, checkOut));
    r.setStatus(ReservationStatus.BOOKED);
    r.setRoomTotal(rateService.roomTotal(room, checkIn, checkOut));
    r.setExtrasTotal(0);
    r.setAccessKey(null);
    r.setCreatedAt(LocalDateTime.now());
    return reservations.save(r);
  }

  @Transactional
  public Reservation addService(String reservationId, String serviceTypeRaw) {
    Reservation r = getOrThrow(reservationId);
    if (r.getStatus() != ReservationStatus.BOOKED
        && r.getStatus() != ReservationStatus.CHECKED_IN) {
      throw new ConflictException("services only allowed while BOOKED or CHECKED_IN");
    }
    ServiceType type = parseServiceType(serviceTypeRaw);
    Room room = roomService.findByNumber(r.getRoomNumber()).orElseThrow();
    additionalServiceService.addExtra(r, type);
    r.setRoomTotal(rateService.roomTotal(room, r.getCheckIn(), r.getCheckOut()));
    return reservations.save(r);
  }

  @Transactional
  public Reservation checkIn(String reservationId) {
    Reservation r = getOrThrow(reservationId);
    if (r.getStatus() != ReservationStatus.BOOKED) {
      throw new ConflictException("only BOOKED reservations can check in");
    }
    r.setStatus(ReservationStatus.CHECKED_IN);
    r.setAccessKey(accessService.generateKey());
    return reservations.save(r);
  }

  @Transactional
  public Invoice checkOut(String reservationId) {
    Reservation r = getOrThrow(reservationId);
    if (r.getStatus() != ReservationStatus.CHECKED_IN) {
      throw new ConflictException("only CHECKED_IN reservations can check out");
    }
    r.setStatus(ReservationStatus.CHECKED_OUT);
    reservations.save(r);
    Room room = roomService.findByNumber(r.getRoomNumber()).orElseThrow();
    return billingService.buildInvoice(r, room);
  }

  @Transactional(readOnly = true)
  public Reservation getReservation(String id) {
    return getOrThrow(id);
  }

  @Transactional(readOnly = true)
  public List<Room> findAvailable(String checkInRaw, String checkOutRaw, String typeRaw) {
    LocalDate checkIn = parseDate(checkInRaw, "checkIn");
    LocalDate checkOut = parseDate(checkOutRaw, "checkOut");
    if (!checkOut.isAfter(checkIn)) {
      throw new BadRequestException("checkOut must be after checkIn");
    }
    RoomType type = typeRaw == null || typeRaw.isBlank() ? null : parseRoomType(typeRaw);
    return roomService.findAvailable(checkIn, checkOut, type);
  }

  @Transactional(readOnly = true)
  public List<Room> findAllRooms() {
    return roomService.findAll();
  }

  private Reservation getOrThrow(String id) {
    return reservations.findById(id)
        .orElseThrow(() -> new NotFoundException("reservation not found"));
  }

  private Room pickRoom(RoomType type, LocalDate checkIn, LocalDate checkOut, Integer wanted) {
    if (wanted != null) {
      Room room = roomService.findByNumber(wanted)
          .orElseThrow(() -> new ConflictException("room not available"));
      if (room.getType() != type) {
        throw new BadRequestException("roomNumber does not match roomType");
      }
      if (!roomService.isAvailable(wanted, checkIn, checkOut)) {
        throw new ConflictException("room not available");
      }
      return room;
    }
    return roomService.findAvailable(checkIn, checkOut, type).stream()
        .findFirst()
        .orElseThrow(() -> new ConflictException("no room available"));
  }

  private RoomType parseRoomType(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("roomType is required");
    }
    try {
      return RoomType.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("invalid roomType");
    }
  }

  private ServiceType parseServiceType(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("serviceType is required");
    }
    try {
      return ServiceType.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("invalid serviceType");
    }
  }

  private LocalDate parseDate(String raw, String field) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException(field + " is required");
    }
    try {
      return LocalDate.parse(raw.trim());
    } catch (Exception e) {
      throw new BadRequestException("invalid date for " + field);
    }
  }
}
