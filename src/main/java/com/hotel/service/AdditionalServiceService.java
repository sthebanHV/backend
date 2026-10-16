package com.hotel.service;

import com.hotel.exception.ConflictException;
import com.hotel.model.ExtraItem;
import com.hotel.model.Reservation;
import com.hotel.model.ServiceType;
import java.util.Optional;
import org.springframework.stereotype.Service;

// Flat SPA/TRANSFER, BREAKFAST charged per night and only once.
@Service
public class AdditionalServiceService {

  public ExtraItem addExtra(Reservation reservation, ServiceType type) {
    Optional<ExtraItem> existing = reservation.getExtras().stream()
        .filter(e -> e.getType() == type).findFirst();
    if (type == ServiceType.BREAKFAST) {
      if (existing.isPresent()) {
        throw new ConflictException("breakfast already added");
      }
      double subtotal = 15.0 * reservation.getNights();
      ExtraItem item = new ExtraItem(type, 15.0, reservation.getNights(), subtotal);
      item.setReservation(reservation);
      reservation.getExtras().add(item);
      refreshTotal(reservation);
      return item;
    }
    double unit = type == ServiceType.SPA ? 50.0 : 30.0;
    if (existing.isPresent()) {
      ExtraItem item = existing.get();
      item.setQuantity(item.getQuantity() + 1);
      item.setSubtotal(item.getUnitPrice() * item.getQuantity());
    } else {
      ExtraItem item = new ExtraItem(type, unit, 1, unit);
      item.setReservation(reservation);
      reservation.getExtras().add(item);
    }
    refreshTotal(reservation);
    return reservation.getExtras().stream()
        .filter(e -> e.getType() == type).findFirst().orElseThrow();
  }

  private void refreshTotal(Reservation reservation) {
    double total = reservation.getExtras().stream().mapToDouble(ExtraItem::getSubtotal).sum();
    reservation.setExtrasTotal(total);
  }
}
