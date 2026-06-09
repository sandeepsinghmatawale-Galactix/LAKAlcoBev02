package com.barinventory.inventory.repos;
 

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barinventory.inventory.entities.BarProductPricing;

@Repository
public interface BarProductPricingRepository extends JpaRepository<BarProductPricing, Long> {
    
    List<BarProductPricing> findByBarId(Long barId);
    
    Optional<BarProductPricing> findByBarIdAndDepotBrandSizeId(Long barId, Long depotBrandSizeId);
}