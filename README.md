# Hotel Reservation Backend

Spring Boot 3 API for a small hotel (15 rooms). Default profile uses H2
in-memory; `prod` profile uses PostgreSQL from env vars.

## Facade and subsystems

`HotelController` talks only to `HotelFacade`, which coordinates 5 services:

- `RoomService` — rooms and availability by dates and type.
- `RateService` — nightly price by season (HIGH: Dec, Jan, Jun, Jul x1.5).
- `AdditionalServiceService` — extras (SPA 50 flat, BREAKFAST 15/night once,
  TRANSFER 30 flat).
- `BillingService` — final invoice with one room line per season block.
- `AccessService` — digital key (UUID) issued at check-in.

## Endpoints (base `/api/hotel`)

| Method | Path | Notes |
|---|---|---|
| POST | `/reservar` | `{guestName, guestEmail, roomType, checkIn, checkOut, roomNumber?}` → 201 |
| GET | `/disponibilidad?checkIn=&checkOut=&type=` | type optional |
| POST | `/servicios/{reservationId}` | `{serviceType}` |
| PUT | `/checkin/{reservationId}` | sets CHECKED_IN + accessKey |
| PUT | `/checkout/{reservationId}` | returns invoice |
| GET | `/reserva/{reservationId}` | single reservation |
| GET | `/habitaciones` | all 15 rooms |

## How to run

```bash
mvn test
mvn package -DskipTests
mvn spring-boot:run
# prod (Railway Postgres):
# SPRING_DATASOURCE_URL=jdbc:postgresql://... SPRING_DATASOURCE_USERNAME=.. \
#   SPRING_DATASOURCE_PASSWORD=.. java -Dspring.profiles.active=prod -jar target/*.jar
```
