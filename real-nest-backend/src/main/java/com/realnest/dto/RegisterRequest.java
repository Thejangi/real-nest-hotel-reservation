package com.realnest.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String password;
    private List<String> roles;
    private String agencyName;
    private String licenseNumber;
    private boolean prepaymentRequired;
    private BigDecimal discountPercentage;
}
