package com.barinventory.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.barinventory.entities.InventorySession;
import com.barinventory.entities.StockroomInventory;

@Repository
public interface StockroomInventoryRepository 
        extends JpaRepository<StockroomInventory, Long> {
	
	List<StockroomInventory> findBySessionSessionId(Long sessionId);
	
	Optional<StockroomInventory>
	findBySessionSessionIdAndBrandSizeBrandSizeId(
	        Long sessionId,
	        Long brandSizeId
	);
	
	@Query("""
			SELECT s
			FROM StockroomInventory s
			WHERE s.session.sessionId = :sessionId
			""")
			List<StockroomInventory> getPreviousSessionStocks(Long sessionId);
	
	@Query("""
			SELECT s
			FROM StockroomInventory s
			WHERE s.session.sessionId = :sessionId
			AND s.saleStock > 0
			""")
			List<StockroomInventory> findDistributableStocks(Long sessionId);
	
	@Query("""
	        SELECT s
	        FROM StockroomInventory s
	        JOIN FETCH s.brandSize bs
	        JOIN FETCH bs.brand
	        WHERE s.session.sessionId = :sessionId
	        """)
	List<StockroomInventory> findBySessionWithBrandSize(Long sessionId);
	
	List<StockroomInventory> findByBarBarIdAndSessionSessionId(Long barId, Long sessionId);
	
	boolean existsByBarBarIdAndSessionSessionIdAndBrandSizeBrandSizeId(
	        Long barId,
	        Long sessionId,
	        Long brandSizeId
	);
	Optional<StockroomInventory>
	findByBarBarIdAndSessionSessionIdAndBrandSizeBrandSizeId(
	        Long barId,
	        Long sessionId,
	        Long brandSizeId
	);

}
