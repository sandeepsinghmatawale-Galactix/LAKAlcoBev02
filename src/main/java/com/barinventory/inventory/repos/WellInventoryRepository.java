package com.barinventory.inventory.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barinventory.inventory.entities.WellInventory;

import jakarta.persistence.LockModeType;

@Repository
public interface WellInventoryRepository extends JpaRepository<WellInventory, Long> {
	
    // ✅ FIX: Traverses through productPricing relationship to evaluate brandSizeId
    boolean existsByWellWellIdAndProductPricingBrandSizeId(
            Long wellId,
            Long brandSizeId
    );

    // ✅ FIX: Aligned with flat barId and nested brandSizeId properties
    Optional<WellInventory> findByBarIdAndSessionSessionIdAndWellWellIdAndProductPricingBrandSizeId(
            Long barId,
            Long sessionId,
            Long wellId,
            Long brandSizeId
    );

    List<WellInventory> findByWellWellIdAndSessionSessionId(Long wellId, Long sessionId);

    List<WellInventory> findBySessionSessionId(Long sessionId);

    @Query("SELECT wi FROM WellInventory wi JOIN FETCH wi.well WHERE wi.session.sessionId = :sessionId")
    List<WellInventory> findBySessionSessionIdWithWell(@Param("sessionId") Long sessionId);

    // ✅ FIX: Changed property path matching name from BarBarId to BarId
    List<WellInventory> findByBarIdAndSessionSessionId(Long barId, Long sessionId);

    // ✅ FIX: Changed property path matching name from BarBarId to BarId
    List<WellInventory> findByBarIdAndSessionSessionIdAndWellWellId(Long barId, Long sessionId, Long wellId);

    // ✅ FIX: Adjusted inner column selection references from `wi.bar.barId` to the flat primitive attribute `wi.barId`
    @Query("""
            SELECT wi FROM WellInventory wi
            WHERE wi.barId = :barId
              AND wi.well.wellId = :wellId
              AND wi.session.sessionId = (
                  SELECT MAX(wi2.session.sessionId)
                  FROM WellInventory wi2
                  WHERE wi2.barId = :barId
                    AND wi2.well.wellId = :wellId
                    AND wi2.session.sessionId < :currentSessionId
              )
            """)
    List<WellInventory> getPreviousWellInventory(
            @Param("barId") Long barId, 
            @Param("wellId") Long wellId,
            @Param("currentSessionId") Long currentSessionId
    );

    // ✅ FIX: Adjusted selection paths to match your entity field map settings
    @Query("""
            SELECT wi FROM WellInventory wi
            WHERE wi.barId = :barId
              AND wi.session.sessionId = :sessionId
              AND wi.well.wellId = :wellId
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WellInventory> lockAndFindByBarSessionWell(
            @Param("barId") Long barId, 
            @Param("sessionId") Long sessionId,
            @Param("wellId") Long wellId
    );
}