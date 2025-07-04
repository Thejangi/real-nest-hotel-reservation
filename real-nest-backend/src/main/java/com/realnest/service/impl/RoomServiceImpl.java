package com.realnest.service.impl;

import com.realnest.entity.Room;
import com.realnest.repository.RoomRepository;
import com.realnest.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public Room createRoom(Room room) {
        System.out.println("Creating room: " + room.getRoomNumber());
        return roomRepository.save(room);
    }

    @Override
    public Room getRoomById(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    @Override
    public Room updateRoom(Long id, Room roomDetails) {
        Room room = getRoomById(id);
        if (room != null) {
            room.setRoomNumber(roomDetails.getRoomNumber());
            room.setType(roomDetails.getType());
            room.setPrice(roomDetails.getPrice());
            room.setCapacity(roomDetails.getCapacity());
            room.setAvailable(roomDetails.getAvailable());
            room.setDescription(roomDetails.getDescription());

            System.out.println("Updating room: " + room.getRoomNumber());
            return roomRepository.save(room);
        }
        return null;
    }

    @Override
    public void deleteRoom(Long id) {
        System.out.println("Deleting room with ID: " + id);
        roomRepository.deleteById(id);
    }

    @Override
    public List<Room> getAllRooms() {
        System.out.println("Getting all rooms from database...");
        List<Room> rooms = roomRepository.findAll();
        System.out.println("Found " + rooms.size() + " rooms");
        return rooms;
    }
}