package com.realnest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_agencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;
    private String phone;
    private String address;

    @Column(name = "prepayment_required")
    private boolean prepaymentRequired;

    @Column(name = "discount_percentage")
    private double discountPercentage;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
