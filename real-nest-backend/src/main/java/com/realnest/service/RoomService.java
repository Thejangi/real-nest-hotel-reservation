package com.realnest.service;

import com.realnest.entity.Room;
import java.util.List;

public interface RoomService {
    Room createRoom(Room room);

    Room getRoomById(Long id);

    Room updateRoom(Long id, Room roomDetails);

    void deleteRoom(Long id);

    List<Room> getAllRooms();
}