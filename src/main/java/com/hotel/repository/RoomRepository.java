package com.hotel.repository;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
  List<Room> findByTypeOrderByNumberAsc(RoomType type);
}
