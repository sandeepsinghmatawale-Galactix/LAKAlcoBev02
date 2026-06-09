package com.barinventory.auth.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barinventory.auth.entities.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByBrandName(String brandName);
    
    List<Brand> findByBarBarId(Long barId);

}