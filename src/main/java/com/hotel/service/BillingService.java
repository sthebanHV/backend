package com.hotel.service;

import com.hotel.model.ExtraItem;
import com.hotel.model.Invoice;
import com.hotel.model.InvoiceLine;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

// Builds the final invoice with one room line per season block.
@Service
public class BillingService {

  private final AtomicLong invoiceSeq = new AtomicLong(1000);
  private final RateService rateService;

  public BillingService(RateService rateService) {
    this.rateService = rateService;
  }

  public Invoice buildInvoice(Reservation reservation, Room room) {
    List<InvoiceLine> lines = new ArrayList<>();
    long highNights = rateService.countHighNights(reservation.getCheckIn(), reservation.getCheckOut());
    long lowNights = reservation.getNights() - highNights;
    if (highNights > 0) {
      double unit = room.getBasePrice() * 1.5;
      lines.add(new InvoiceLine(
          highNights + (highNights == 1 ? " night " : " nights ") + room.getType() + " (HIGH season)",
          (int) highNights, unit, unit * highNights));
    }
    if (lowNights > 0) {
      lines.add(new InvoiceLine(
          lowNights + (lowNights == 1 ? " night " : " nights ") + room.getType() + " (LOW season)",
          (int) lowNights, room.getBasePrice(), room.getBasePrice() * lowNights));
    }
    for (ExtraItem e : reservation.getExtras()) {
      String desc = switch (e.getType()) {
        case SPA -> "SPA x" + e.getQuantity();
        case TRANSFER -> "TRANSFER x" + e.getQuantity();
        case BREAKFAST -> "BREAKFAST x" + e.getQuantity() + " nights";
      };
      lines.add(new InvoiceLine(desc, e.getQuantity(), e.getUnitPrice(), e.getSubtotal()));
    }
    Invoice invoice = new Invoice();
    invoice.setInvoiceNumber("INV-" + invoiceSeq.incrementAndGet());
    invoice.setReservationId(reservation.getId());
    invoice.setGuestName(reservation.getGuestName());
    invoice.setRoomNumber(reservation.getRoomNumber());
    invoice.setRoomType(reservation.getRoomType());
    invoice.setCheckIn(reservation.getCheckIn());
    invoice.setCheckOut(reservation.getCheckOut());
    invoice.setNights(reservation.getNights());
    invoice.setLines(lines);
    invoice.setRoomTotal(reservation.getRoomTotal());
    invoice.setExtrasTotal(reservation.getExtrasTotal());
    invoice.setTotal(reservation.getRoomTotal() + reservation.getExtrasTotal());
    invoice.setIssuedAt(LocalDateTime.now());
    return invoice;
  }
}
