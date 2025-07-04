package com.realnest.repository;

import com.realnest.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // JpaRepository provides all the standard CRUD methods:
    // - save(Room room)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // - delete(Room room)
    // - existsById(Long id)
    // - count()
    // etc.

    // Explicit query to ensure we're using the correct table
    @Query("SELECT r FROM Room r")
    List<Room> findAllRooms();

    // You can add custom query methods here if needed
    // For example:
    // List<Room> findByType(Room.RoomType type);
    // List<Room> findByAvailableTrue();
    // List<Room> findByPriceLessThan(BigDecimal price);
}