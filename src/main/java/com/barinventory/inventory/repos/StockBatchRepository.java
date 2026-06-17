package com.barinventory.inventory.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barinventory.inventory.entities.StockBatch;

public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

	@Query("SELECT b FROM StockBatch b WHERE b.barId = :barId AND b.depotPackId = :packId "
			+ "AND b.quantityRemaining > 0 ORDER BY b.receivedAt ASC")
	List<StockBatch> findRemainingBatchesFifo(@Param("barId") Long barId, @Param("packId") Long packId);

	@Query("SELECT COALESCE(SUM(b.quantityRemaining), 0) FROM StockBatch b WHERE b.barId = :barId AND b.depotPackId = :packId")
	Integer getStockOnHand(@Param("barId") Long barId, @Param("packId") Long packId);

	List<StockBatch> findByBarIdAndDepotPackIdOrderByReceivedAtDesc(Long barId, Long depotPackId);
	
 
	Optional<StockBatch> findByBarIdAndDepotPackId(Long barId, Long depotPackId);
	
	
}