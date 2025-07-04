package com.realnest.repository;

import com.realnest.entity.TravelAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelAgencyRepository extends JpaRepository<TravelAgency, Long> {
    boolean existsByEmail(String email);
}
