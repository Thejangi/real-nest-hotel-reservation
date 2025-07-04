package com.realnest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms") // Explicitly use "rooms" table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(name = "room_number", nullable = false, unique = true, length = 50)
       private String roomNumber;

       @Enumerated(EnumType.STRING)
       @Column(name = "type", nullable = false)
       private RoomType type;

       @Column(name = "price", nullable = false, precision = 10, scale = 2)
       private BigDecimal price;

       @Column(name = "capacity", nullable = false)
       private Integer capacity;

       @Column(name = "available", nullable = false)
       private Boolean available = true;

       @Column(name = "description", columnDefinition = "TEXT")
       private String description;

       // Enum for room types
       public enum RoomType {
              SINGLE, DOUBLE, SUITE, DELUXE
       }
}