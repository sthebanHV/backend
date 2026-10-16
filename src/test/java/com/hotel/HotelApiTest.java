package com.hotel;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelApiTest {

  @Autowired
  private MockMvc mvc;

  private String reserve(String body) throws Exception {
    String res = mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andReturn().getResponse().getContentAsString();
    return res.split("\"id\":\"")[1].split("\"")[0];
  }

  @Test
  void seedsFifteenRooms() throws Exception {
    mvc.perform(get("/api/hotel/habitaciones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(15)));
  }

  @Test
  void availabilityFiltersByType() throws Exception {
    mvc.perform(get("/api/hotel/disponibilidad")
            .param("checkIn", "2026-03-10").param("checkOut", "2026-03-12")
            .param("type", "SINGLE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(6)));
  }

  @Test
  void lowSeasonPricingMarch() throws Exception {
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"Low\",\"guestEmail\":\"low@x.com\","
                + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-03-10\","
                + "\"checkOut\":\"2026-03-12\",\"roomNumber\":101}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nights", is(2)))
        .andExpect(jsonPath("$.roomTotal", is(160.0)));
  }

  @Test
  void highSeasonPricingJuly() throws Exception {
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"High\",\"guestEmail\":\"high@x.com\","
                + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-07-10\","
                + "\"checkOut\":\"2026-07-12\",\"roomNumber\":102}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nights", is(2)))
        .andExpect(jsonPath("$.roomTotal", is(240.0)));
  }

  @Test
  void breakfastChargedPerNightAndOnlyOnce() throws Exception {
    String id = reserve("{\"guestName\":\"B\",\"guestEmail\":\"b@x.com\","
        + "\"roomType\":\"DOUBLE\",\"checkIn\":\"2026-03-10\","
        + "\"checkOut\":\"2026-03-13\",\"roomNumber\":201}");
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"BREAKFAST\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.extras[0].quantity", is(3)))
        .andExpect(jsonPath("$.extras[0].unitPrice", is(15.0)))
        .andExpect(jsonPath("$.extrasTotal", is(45.0)));
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"BREAKFAST\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void flatExtrasCanRepeat() throws Exception {
    String id = reserve("{\"guestName\":\"S\",\"guestEmail\":\"s@x.com\","
        + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-04-10\","
        + "\"checkOut\":\"2026-04-11\",\"roomNumber\":103}");
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"SPA\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.extrasTotal", is(50.0)));
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"SPA\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.extrasTotal", is(100.0)));
  }

  @Test
  void fullStayCheckInOutAndInvoiceTotals() throws Exception {
    String id = reserve("{\"guestName\":\"Inv\",\"guestEmail\":\"inv@x.com\","
        + "\"roomType\":\"SUITE\",\"checkIn\":\"2026-07-01\","
        + "\"checkOut\":\"2026-07-04\",\"roomNumber\":301}");
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"BREAKFAST\"}"))
        .andExpect(status().isOk());
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"SPA\"}"))
        .andExpect(status().isOk());
    mvc.perform(put("/api/hotel/checkin/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("CHECKED_IN")))
        .andExpect(jsonPath("$.accessKey", notNullValue()));
    mvc.perform(put("/api/hotel/checkout/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomTotal", is(1125.0)))
        .andExpect(jsonPath("$.extrasTotal", is(95.0)))
        .andExpect(jsonPath("$.total", is(1220.0)));
  }

  @Test
  void getReservation() throws Exception {
    String id = reserve("{\"guestName\":\"G\",\"guestEmail\":\"g@x.com\","
        + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-05-10\","
        + "\"checkOut\":\"2026-05-11\",\"roomNumber\":104}");
    mvc.perform(get("/api/hotel/reserva/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guestName", is("G")));
  }

  @Test
  void unknownReservationIs404() throws Exception {
    mvc.perform(get("/api/hotel/reserva/R-999999"))
        .andExpect(status().isNotFound());
    mvc.perform(put("/api/hotel/checkin/R-999999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void doubleBookingSameRoomIs409() throws Exception {
    reserve("{\"guestName\":\"A\",\"guestEmail\":\"a@x.com\","
        + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-08-10\","
        + "\"checkOut\":\"2026-08-12\",\"roomNumber\":105}");
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"B2\",\"guestEmail\":\"b2@x.com\","
                + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-08-11\","
                + "\"checkOut\":\"2026-08-13\",\"roomNumber\":105}"))
        .andExpect(status().isConflict());
  }

  @Test
  void wrongStatusTransitionsAre409() throws Exception {
    String id = reserve("{\"guestName\":\"W\",\"guestEmail\":\"w@x.com\","
        + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-09-10\","
        + "\"checkOut\":\"2026-09-11\",\"roomNumber\":106}");
    mvc.perform(put("/api/hotel/checkout/" + id))
        .andExpect(status().isConflict());
    mvc.perform(put("/api/hotel/checkin/" + id))
        .andExpect(status().isOk());
    mvc.perform(put("/api/hotel/checkin/" + id))
        .andExpect(status().isConflict());
    mvc.perform(put("/api/hotel/checkout/" + id))
        .andExpect(status().isOk());
    mvc.perform(post("/api/hotel/servicios/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"serviceType\":\"SPA\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void badDatesAndTypesAre400() throws Exception {
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"X\",\"guestEmail\":\"x@x.com\","
                + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-03-12\","
                + "\"checkOut\":\"2026-03-10\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"X\",\"guestEmail\":\"x@x.com\","
                + "\"roomType\":\"PENTHOUSE\",\"checkIn\":\"2026-03-10\","
                + "\"checkOut\":\"2026-03-12\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/hotel/disponibilidad")
            .param("checkIn", "2026-03-12").param("checkOut", "2026-03-10"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/hotel/reservar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"guestName\":\"\",\"guestEmail\":\"x@x.com\","
                + "\"roomType\":\"SINGLE\",\"checkIn\":\"2026-03-10\","
                + "\"checkOut\":\"2026-03-12\"}"))
        .andExpect(status().isBadRequest());
  }
}
