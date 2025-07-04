package com.realnest.repository;

import com.realnest.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // You can add custom queries like findByEmail(String email) if needed
}
