package com.realnest.controller;

import com.realnest.entity.Room;
import com.realnest.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // Create a Room
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        System.out.println("=== CREATE ROOM REQUEST ===");
        System.out.println("Room Number: " + room.getRoomNumber());
        System.out.println("Room Type: " + room.getType());
        Room createdRoom = roomService.createRoom(room);
        System.out.println("Room created with ID: " + createdRoom.getId());
        return ResponseEntity.ok(createdRoom);
    }

    // Update Room
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        System.out.println("=== UPDATE ROOM REQUEST ===");
        System.out.println("Room ID: " + id);
        System.out.println("Room Number: " + room.getRoomNumber());
        Room updatedRoom = roomService.updateRoom(id, room);
        if (updatedRoom != null) {
            System.out.println("Room updated successfully");
            return ResponseEntity.ok(updatedRoom);
        } else {
            System.out.println("Room not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    // Delete Room
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        System.out.println("=== DELETE ROOM REQUEST ===");
        System.out.println("Room ID: " + id);
        roomService.deleteRoom(id);
        System.out.println("Room deleted successfully");
        return ResponseEntity.noContent().build();
    }

    // Get Single Room
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        System.out.println("=== GET ROOM REQUEST ===");
        System.out.println("Room ID: " + id);
        Room room = roomService.getRoomById(id);
        if (room != null) {
            System.out.println("Room found: " + room.getRoomNumber());
            return ResponseEntity.ok(room);
        } else {
            System.out.println("Room not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    // Get All Rooms
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        System.out.println("=== GET ALL ROOMS REQUEST ===");
        List<Room> rooms = roomService.getAllRooms();
        System.out.println("Returning " + rooms.size() + " rooms");
        for (Room room : rooms) {
            System.out.println("Room: " + room.getRoomNumber() + " - " + room.getType() + " - $" + room.getPrice());
        }
        return ResponseEntity.ok(rooms);
    }
}