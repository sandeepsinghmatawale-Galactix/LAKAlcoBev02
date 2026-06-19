package com.barinventory.inventory.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barinventory.inventory.entities.Well;

@Repository
public interface WellRepository extends JpaRepository<Well, Long> {
	
	List<Well> findAll();
	
	Optional<Well> findByWellName(String wellName);
	
 
	 

	// ✅ FIX: Changed from findByWellIdAndBar_BarId to flat primitive barId lookup
    Optional<Well> findByWellIdAndBarId(Long wellId, Long barId);
    
    // ✅ FIX: Changed from findByBar_BarId to flat primitive barId lookup
    List<Well> findByBarId(Long barId);
    
  
    List<Well> findByBarIdAndActiveTrue(Long barId);
    

    Optional<Well> findByBarIdAndWellNameIgnoreCase(Long barId, String wellName);
    
    long countByBarIdAndActiveTrue(Long barId);
 
}
