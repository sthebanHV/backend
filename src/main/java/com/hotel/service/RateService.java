package com.hotel.service;

import com.hotel.model.Room;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Service;

// Nightly price: HIGH season (Dec, Jan, Jun, Jul) is base x 1.5.
@Service
public class RateService {

  private static final Set<Integer> HIGH_MONTHS = Set.of(12, 1, 6, 7);

  public boolean isHighSeason(LocalDate date) {
    return HIGH_MONTHS.contains(date.getMonthValue());
  }

  public double priceForNight(Room room, LocalDate date) {
    return isHighSeason(date) ? room.getBasePrice() * 1.5 : room.getBasePrice();
  }

  public double roomTotal(Room room, LocalDate checkIn, LocalDate checkOut) {
    double total = 0;
    for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
      total += priceForNight(room, d);
    }
    return total;
  }

  public long countHighNights(LocalDate checkIn, LocalDate checkOut) {
    return checkIn.datesUntil(checkOut).filter(this::isHighSeason).count();
  }
}
