package com.barinventory.inventory.repos;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.barinventory.inventory.entities.StockroomInventory;

@Repository
public interface StockroomInventoryRepository extends JpaRepository<StockroomInventory, Long> {
    
    List<StockroomInventory> findBySessionSessionId(Long sessionId);
    
    // ✅ Fixed: BrandSize brandSize relationship replaced by primitive brandSizeId
    Optional<StockroomInventory> findBySessionSessionIdAndBrandSizeId(Long sessionId, Long brandSizeId);
    
    @Query("SELECT s FROM StockroomInventory s WHERE s.session.sessionId = :sessionId")
    List<StockroomInventory> getPreviousSessionStocks(@Param("sessionId") Long sessionId);
    
    @Query("SELECT s FROM StockroomInventory s WHERE s.session.sessionId = :sessionId AND s.saleStock > 0")
    List<StockroomInventory> findDistributableStocks(@Param("sessionId") Long sessionId);
    
    // ✅ Fixed: Removed toxic cross-package fetch strategy. 
    // Instead, we join onto your local pricing metadata layout cache for rapid execution!
    @Query("""
    	       SELECT s FROM StockroomInventory s
    	       JOIN BarProductPricing bpp ON s.brandSizeId = bpp.brandSizeId AND s.barId = bpp.barId
    	       WHERE s.session.sessionId = :sessionId
    	       """)
    	List<StockroomInventory> findBySessionWithBrandSize(@Param("sessionId") Long sessionId);
    
    List<StockroomInventory> findByBarIdAndSessionSessionId(Long barId, Long sessionId);
    
    boolean existsByBarIdAndSessionSessionIdAndBrandSizeId(Long barId, Long sessionId, Long brandSizeId);
    
    Optional<StockroomInventory> findByBarIdAndSessionSessionIdAndBrandSizeId(Long barId, Long sessionId, Long brandSizeId);
}