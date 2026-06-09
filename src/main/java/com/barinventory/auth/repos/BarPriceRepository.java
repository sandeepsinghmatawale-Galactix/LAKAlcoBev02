package com.barinventory.auth.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.auth.entities.BarPrice;

public interface BarPriceRepository extends JpaRepository<BarPrice, Long> {
	
	Optional<BarPrice> findByBarBarIdAndBrandBrandId(Long barId, Long brandId);
	
 
	
	   List<BarPrice> findByBarBarId(Long barId);

}
