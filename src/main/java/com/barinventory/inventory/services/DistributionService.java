package com.barinventory.inventory.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.barinventory.inventory.dtos.DistributionRequest;
import com.barinventory.inventory.entities.Distribution;
import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.entities.WellDistribution;
import com.barinventory.inventory.repos.DistributionRepository;
import com.barinventory.inventory.repos.InventorySessionRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;
import com.barinventory.inventory.repos.WellDistributionRepository;
import com.barinventory.inventory.repos.WellRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DistributionService {

    private final DistributionRepository distributionRepo;
    private final WellDistributionRepository wellDistributionRepo;
    private final StockroomInventoryRepository stockroomRepo;
    private final WellRepository wellRepo;
    private final InventorySessionRepository sessionRepo;

    /*
     * ----------------------------------------- CREATE DISTRIBUTION
     * -----------------------------------------
     */
    public Distribution createDistribution(Long barId, Long sessionId) {
        InventorySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getBar().getBarId().equals(barId)) { // ✅ Fixed: Flat ID verification
            throw new RuntimeException("Session does not belong to this bar");
        }

        Distribution distribution = new Distribution();
        distribution.setSession(session);
        distribution.setDistributedAt(LocalDateTime.now());

        return distributionRepo.save(distribution);
    }

    /*
     * ----------------------------------------- MAIN DISTRIBUTION
     * -----------------------------------------
     */
    public void distributeStock(Long distributionId, List<DistributionRequest> requests) {
        validateInput(requests);

        Distribution distribution = distributionRepo.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Distribution not found"));

        Long sessionId = distribution.getSession().getSessionId();
        List<StockroomInventory> stocks = stockroomRepo.findDistributableStocks(sessionId);

        validateAgainstStock(requests, stocks);

        List<WellDistribution> batchList = prepareBatch(requests, distribution);

        wellDistributionRepo.deleteByDistributionId(distributionId);
        wellDistributionRepo.saveAll(batchList);
        wellDistributionRepo.flush();

        validateDistribution(sessionId, distributionId);
    }

    private void validateInput(List<DistributionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("No distribution data submitted");
        }
        for (DistributionRequest r : requests) {
            if (r.getDistributedQty() == null || r.getDistributedQty() <= 0) continue;
            if (r.getBrandSizeId() == null || r.getBrandSizeId() == 0) continue;
            if (r.getWellId() == null || r.getWellId() == 0) continue;
            if (r.getDistributedQty() < 0) {
                throw new RuntimeException("Negative quantity not allowed");
            }
        }
    }

    /*
     * ----------------------------------------- STOCK VALIDATION
     * -----------------------------------------
     */
    private void validateAgainstStock(List<DistributionRequest> requests, List<StockroomInventory> stocks) {
        Map<Long, Integer> totalMap = new HashMap<>();

        for (DistributionRequest r : requests) {
            if (r.getDistributedQty() == null || r.getDistributedQty() <= 0) continue;
            if (r.getBrandSizeId() == null || r.getBrandSizeId() == 0) continue;
            totalMap.merge(r.getBrandSizeId(), r.getDistributedQty(), Integer::sum);
        }

        for (StockroomInventory stock : stocks) {
            if (stock.getSaleStock() == null || stock.getSaleStock() == 0) continue;

            Long brandSizeId = stock.getBrandSizeId(); // ✅ Fixed: Using flat primitive property lookup
            int actual = totalMap.getOrDefault(brandSizeId, 0);

            if (actual != stock.getSaleStock()) {
                throw new RuntimeException("Distribution validation mismatch for Brand Size variant reference target ID [ " 
                        + brandSizeId + " ] | Expected Volume Count=" + stock.getSaleStock() + " | Calculated Distributed Total=" + actual);
            }
        }
    }

    /*
     * ----------------------------------------- PREPARE BATCH
     * -----------------------------------------
     */
    private List<WellDistribution> prepareBatch(List<DistributionRequest> requests, Distribution distribution) {
        List<WellDistribution> list = new ArrayList<>();

        for (DistributionRequest r : requests) {
            if (r.getDistributedQty() == null || r.getDistributedQty() <= 0) continue;
            if (r.getBrandSizeId() == null || r.getWellId() == null) continue;

            WellDistribution wd = new WellDistribution();
            wd.setDistribution(distribution);
            
            // ✅ Fixed: Zero database context crossover. Directly sets primitive entity table layout columns.
            wd.setBrandSizeId(r.getBrandSizeId()); 
            wd.setWell(wellRepo.getReferenceById(r.getWellId())); // Well configuration remains localized inside Inventory module boundaries
            wd.setDistributedQty(r.getDistributedQty());
            wd.setDistributedAt(LocalDateTime.now());

            list.add(wd);
        }
        return list;
    }

    /*
     * ----------------------------------------- FINAL VALIDATION
     * -----------------------------------------
     */
    private void validateDistribution(Long sessionId, Long distributionId) {
        List<StockroomInventory> stocks = stockroomRepo.findDistributableStocks(sessionId);

        for (StockroomInventory stock : stocks) {
            if (stock.getSaleStock() == null || stock.getSaleStock() == 0) continue;

            Integer distributedQty = wellDistributionRepo.getTotalDistributedQty(distributionId, stock.getBrandSizeId()); // ✅ Fixed

            if (distributedQty == null) {
                distributedQty = 0;
            }

            if (!distributedQty.equals(stock.getSaleStock())) {
                throw new RuntimeException("Final transaction alignment verification step rejected. Identity SKU reference total [ " 
                        + stock.getBrandSizeId() + " ] does not match the session ledger values.");
            }
        }
    }

    public Long getSessionIdByDistribution(Long distributionId) {
        return distributionRepo.findById(distributionId).map(d -> d.getSession().getSessionId())
                .orElseThrow(() -> new RuntimeException("Distribution not found"));
    }
}