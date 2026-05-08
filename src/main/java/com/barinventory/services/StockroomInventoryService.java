package com.barinventory.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.barinventory.dtos.StockroomClosingRequest;
import com.barinventory.entities.InventorySession;
import com.barinventory.entities.StockroomInventory;
import com.barinventory.repos.DistributionRepository;
import com.barinventory.repos.InventorySessionRepository;
import com.barinventory.repos.StockroomInventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockroomInventoryService {

	private final StockroomInventoryRepository stockroomRepo;

	private final InventorySessionRepository sessionRepo;

	private final DistributionRepository distributionRepo;

	/*
	 * ----------------------------------------- INITIALIZE STOCKROOM
	 * -----------------------------------------
	 */
	public void initializeStockroom(Long currentSessionId, Long previousSessionId) {

		List<StockroomInventory> previousStocks = stockroomRepo.findBySessionSessionId(previousSessionId);

		InventorySession currentSession = sessionRepo.findById(currentSessionId).orElseThrow();

		for (StockroomInventory previous : previousStocks) {

			boolean exists = stockroomRepo.existsByBarBarIdAndSessionSessionIdAndBrandSizeBrandSizeId(
					currentSession.getBar().getBarId(), currentSessionId, previous.getBrandSize().getBrandSizeId());

			if (exists) {
				continue;
			}

			StockroomInventory current = new StockroomInventory();

			current.setSession(currentSession);

			current.setBar(currentSession.getBar());

			current.setBrandSize(previous.getBrandSize());

			current.setOpeningStock(previous.getClosingStock());

			current.setReceivedStock(0);

			current.setClosingStock(0);

			current.setSaleStock(0);

			stockroomRepo.save(current);
		}
	}

	/*
	 * ----------------------------------------- SAVE
	 * -----------------------------------------
	 */
	public void save(StockroomInventory stock) {

		stockroomRepo.save(stock);
	}

	/*
	 * ----------------------------------------- GET STOCKROOM
	 * -----------------------------------------
	 */
	public List<StockroomInventory> getStockroomByBarAndSession(Long barId, Long sessionId) {

		return stockroomRepo.findByBarBarIdAndSessionSessionId(barId, sessionId);
	}

	public List<StockroomInventory> getStockroomBySession(Long sessionId) {

		return stockroomRepo.findBySessionSessionId(sessionId);
	}

	/*
	 * ----------------------------------------- UPDATE CLOSING
	 * -----------------------------------------
	 */
	public void updateStockroomClosing(Long barId, Long sessionId, List<StockroomClosingRequest> requests) {

		InventorySession session = sessionRepo.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if (!session.getBar().getBarId().equals(barId)) {

			throw new RuntimeException("Session does not belong to this bar");
		}

		for (StockroomClosingRequest request : requests) {

			StockroomInventory stock = stockroomRepo
					.findBySessionSessionIdAndBrandSizeBrandSizeId(sessionId, request.getBrandSizeId())
					.orElseThrow(() -> new RuntimeException("Stock not found"));

			int totalAvailable = stock.getOpeningStock() + stock.getReceivedStock();

			if (request.getClosingStock() > totalAvailable) {

				throw new RuntimeException("Invalid closing stock for " + stock.getBrandSize().getBrand().getBrandName()
						+ " " + stock.getBrandSize().getSizeMl() + "ml");
			}

			stock.setClosingStock(request.getClosingStock());

			stock.setSaleStock(totalAvailable - request.getClosingStock());
		}
	}

	/*
	 * ----------------------------------------- SALE STOCK MAP
	 * -----------------------------------------
	 */
	public Map<Long, Integer> getSaleStockMap(Long distributionId) {

		Long sessionId = distributionRepo.findById(distributionId).orElseThrow().getSession().getSessionId();

		List<StockroomInventory> stocks = stockroomRepo.findBySessionSessionId(sessionId);

		Map<Long, Integer> stockMap = new HashMap<>();

		for (StockroomInventory stock : stocks) {
		    int available = stock.getSaleStock() > 0
		        ? stock.getSaleStock()
		        : (stock.getOpeningStock() + stock.getReceivedStock() - stock.getClosingStock());
		    if (available > 0) {
		        stockMap.put(stock.getBrandSize().getBrandSizeId(), available);
		    }
		}
		return stockMap;
	}
}