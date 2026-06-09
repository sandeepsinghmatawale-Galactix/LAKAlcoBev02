package com.barinventory.inventory.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.barinventory.inventory.dtos.StockroomClosingRequest;
import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.repos.DistributionRepository;
import com.barinventory.inventory.repos.InventorySessionRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockroomInventoryService {

    private final StockroomInventoryRepository stockroomRepo;
    private final InventorySessionRepository sessionRepo;
    private final DistributionRepository distributionRepo;

    /*
     * ----------------------------------------- 
     * INITIALIZE STOCKROOM (Carry Forward from Previous Session)
     * -----------------------------------------
     */
    public void initializeStockroom(Long currentSessionId, Long previousSessionId) {
        List<StockroomInventory> previousStocks = stockroomRepo.findBySessionSessionId(previousSessionId);
        InventorySession currentSession = sessionRepo.findById(currentSessionId)
                .orElseThrow(() -> new RuntimeException("Current operational session context missing."));

        Long barId = currentSession.getBar().getBarId(); // ✅ extract once, reuse below

        for (StockroomInventory previous : previousStocks) {

            boolean exists = stockroomRepo.existsByBarIdAndSessionSessionIdAndBrandSizeId(
                    barId, currentSessionId, previous.getBrandSizeId());

            if (exists) {
                continue;
            }

            StockroomInventory current = new StockroomInventory();
            current.setSession(currentSession);
            current.setBarId(barId);
            current.setBrandSizeId(previous.getBrandSizeId());

            current.setOpeningStock(previous.getClosingStock());
            current.setReceivedStock(0);
            current.setClosingStock(0);
            current.setSaleStock(0);

            stockroomRepo.save(current);
        }
    }

    public void save(StockroomInventory stock) {
        stockroomRepo.save(stock);
    }

    public List<StockroomInventory> getStockroomByBarAndSession(Long barId, Long sessionId) {
        return stockroomRepo.findByBarIdAndSessionSessionId(barId, sessionId);
    }

    public List<StockroomInventory> getStockroomBySession(Long sessionId) {
        return stockroomRepo.findBySessionSessionId(sessionId);
    }

    /*
     * ----------------------------------------- 
     * UPDATE CLOSING BALANCES
     * -----------------------------------------
     */
    public void updateStockroomClosing(Long barId, Long sessionId, List<StockroomClosingRequest> requests) {
        InventorySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getBar().getBarId().equals(barId)) {  // ✅ fixed
            throw new RuntimeException("Session execution context mismatch: profile does not match target corporate location.");
        }

        for (StockroomClosingRequest request : requests) {
            StockroomInventory stock = stockroomRepo
                    .findBySessionSessionIdAndBrandSizeId(sessionId, request.getBrandSizeId())
                    .orElseThrow(() -> new RuntimeException("Stock target profile item not located in backroom ledger."));

            int totalAvailable = stock.getOpeningStock() + stock.getReceivedStock();

            if (request.getClosingStock() > totalAvailable) {
                throw new RuntimeException("Invalid closing stock entry for Brand ID Variant [ " + stock.getBrandSizeId() + " ]. "
                        + "Counted quantity exceeds calculated maximum total available unit volume.");
            }

            stock.setClosingStock(request.getClosingStock());
            stock.setSaleStock(totalAvailable - request.getClosingStock());
        }
    }

    /*
     * ----------------------------------------- 
     * SALE STOCK TRACKING MAP
     * -----------------------------------------
     */
    public Map<Long, Integer> getSaleStockMap(Long distributionId) {
        Long sessionId = distributionRepo.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Target distribution transaction window not found."))
                .getSession().getSessionId();

        List<StockroomInventory> stocks = stockroomRepo.findBySessionSessionId(sessionId);
        Map<Long, Integer> stockMap = new HashMap<>();

        for (StockroomInventory stock : stocks) {
            int available = stock.getSaleStock() > 0
                ? stock.getSaleStock()
                : (stock.getOpeningStock() + stock.getReceivedStock() - stock.getClosingStock());
                
            if (available > 0) {
                stockMap.put(stock.getBrandSizeId(), available); // ✅ Using decoupled flat key mapping
            }
        }
        return stockMap;
    }
}