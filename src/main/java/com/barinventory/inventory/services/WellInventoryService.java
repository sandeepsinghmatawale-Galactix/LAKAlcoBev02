package com.barinventory.inventory.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barinventory.inventory.dtos.WellClosingRequest;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.InventoryStatus;
import com.barinventory.inventory.entities.Well;
import com.barinventory.inventory.entities.WellDistribution;
import com.barinventory.inventory.entities.WellInventory;
import com.barinventory.inventory.repos.BarProductPricingRepository;
import com.barinventory.inventory.repos.InventorySessionRepository;
import com.barinventory.inventory.repos.WellDistributionRepository;
import com.barinventory.inventory.repos.WellInventoryRepository;
import com.barinventory.inventory.repos.WellRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WellInventoryService {

    private final WellRepository wellRepo;
    private final WellInventoryRepository wellInventoryRepo;
    private final WellDistributionRepository wellDistributionRepo;
    private final InventorySessionRepository sessionRepo;
    private final BarProductPricingRepository productPricingRepo;

    /*
     * -----------------------------------------
     * INITIALIZE WELL INVENTORY
     * -----------------------------------------
     */
    public void initializeWellInventory(Long barId, Long sessionId, Long wellId) {
    	InventorySession session = sessionRepo.findBySessionIdAndBarBarId(sessionId, barId)
    	        .orElseThrow(() -> new RuntimeException("Session not found"));

     // ✅ FIX: Swapped to findByWellIdAndBarId
        Well well = wellRepo.findByWellIdAndBarId(wellId, barId) 
                .orElseThrow(() -> new RuntimeException("Well profile target not configured on your layouts."));

        // lockAndFindByBarSessionWell uses: @Param("barId"), @Param("sessionId"), @Param("wellId")
        List<WellInventory> existing = wellInventoryRepo.lockAndFindByBarSessionWell(barId, sessionId, wellId);

        boolean alreadyCompleted = !existing.isEmpty()
                && existing.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);

        if (alreadyCompleted) {
            throw new RuntimeException("Well already completed");
        }

        if (!existing.isEmpty()) {
            return;
        }

        // getPreviousWellInventory uses: @Param("barId"), @Param("wellId"), @Param("currentSessionId")
        List<WellInventory> previousInventory = wellInventoryRepo.getPreviousWellInventory(barId, wellId, sessionId);
        List<WellDistribution> distributions = wellDistributionRepo.findByWellSessionAndBar(wellId, sessionId, barId);

        Map<Long, Integer> receivedMap = distributions.stream().collect(Collectors.groupingBy(
                WellDistribution::getBrandSizeId,
                Collectors.summingInt(WellDistribution::getDistributedQty)));

        List<WellInventory> toInsert = new ArrayList<>();

        /*
         * -----------------------------------------
         * PREVIOUS STOCKS (Carry Forward)
         * -----------------------------------------
         */
        for (WellInventory prev : previousInventory) {
            // ✅ FIX: Extract brandSizeId safely via the productPricing object graph
            Long brandSizeId = prev.getProductPricing().getBrandSizeId();

            WellInventory inv = new WellInventory();
            inv.setBarId(barId); // ✅ FIX: Use primitive Long field assignment directly
            inv.setSession(session);
            inv.setWell(well);
            inv.setProductPricing(prev.getProductPricing());
            inv.setOpeningStock(prev.getClosingStock());
            inv.setReceivedStock(receivedMap.getOrDefault(brandSizeId, 0));
            inv.setClosingStock(0);
            inv.setSaleStock(0);
            inv.setAmount(BigDecimal.ZERO); // Initial state calculation
            inv.setStatus(InventoryStatus.IN_PROGRESS);

            toInsert.add(inv);
        }

        Set<Long> existingBrandSizeIds = toInsert.stream()
                .map(i -> i.getProductPricing().getBrandSizeId()) // ✅ FIX: Reference actual Pricing object node
                .collect(Collectors.toSet());

        /*
         * -----------------------------------------
         * NEW STOCKS FROM DISTRIBUTION WINDOWS
         * -----------------------------------------
         */
        for (WellDistribution dist : distributions) {
            Long brandSizeId = dist.getBrandSizeId();

            if (existingBrandSizeIds.contains(brandSizeId)) {
                continue;
            }

            BarProductPricing businessPricingInfo = productPricingRepo.findByBarIdAndDepotBrandSizeId(barId, brandSizeId)
                    .orElseThrow(() -> new RuntimeException("Configuration Error: Active product catalog details missing."));

            WellInventory inv = new WellInventory();
            inv.setBarId(barId); // ✅ FIX: Set primitive Long ID
            inv.setSession(session);
            inv.setWell(well);
            inv.setProductPricing(businessPricingInfo);
            inv.setOpeningStock(0);
            inv.setReceivedStock(receivedMap.getOrDefault(brandSizeId, 0));
            inv.setClosingStock(0);
            inv.setSaleStock(0);
            inv.setAmount(BigDecimal.ZERO); // Initial state calculation
            inv.setStatus(InventoryStatus.IN_PROGRESS);

            toInsert.add(inv);
            existingBrandSizeIds.add(brandSizeId);
        }

        if (!toInsert.isEmpty()) {
            wellInventoryRepo.saveAll(toInsert);
        }
    }

    // ✅ FIX: Aligned to findByBarIdAndSessionSessionIdAndWellWellId
    public List<WellInventory> getWellInventory(Long barId, Long sessionId, Long wellId) {
        return wellInventoryRepo.findByBarIdAndSessionSessionIdAndWellWellId(barId, sessionId, wellId);
    }

    /*
     * -----------------------------------------
     * UPDATE WELL CLOSING & ACCOUNTING
     * -----------------------------------------
     */
    public void updateWellClosing(Long barId, Long sessionId, Long wellId, List<WellClosingRequest> requests) {
    	sessionRepo.findBySessionIdAndBarBarId(sessionId, barId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
     // ✅ FIX: Swapped to findByWellIdAndBarId
        wellRepo.findByWellIdAndBarId(wellId, barId).orElseThrow(() -> new RuntimeException("Well not found"));
        for (WellClosingRequest req : requests) {
            // ✅ FIX: Invokes repository layout criteria method perfectly
            WellInventory inv = wellInventoryRepo
                    .findByBarIdAndSessionSessionIdAndWellWellIdAndProductPricingBrandSizeId(barId, sessionId, wellId, req.getBrandSizeId())
                    .orElseThrow(() -> new RuntimeException("Inventory records not matching configuration maps."));

            int total = inv.getOpeningStock() + inv.getReceivedStock();

            if (req.getClosingStock() > total) {
                throw new RuntimeException("Invalid closing stock value entered.");
            }

            int calculatedSaleStock = total - req.getClosingStock();

            inv.setClosingStock(req.getClosingStock());
            inv.setSaleStock(calculatedSaleStock);
         // ✅ FIX: Convert the Double selling price to BigDecimal before multiplying
            if (inv.getProductPricing().getSellingPrice() != null) {
                inv.setAmount(BigDecimal.valueOf(calculatedSaleStock)
                        .multiply(BigDecimal.valueOf(inv.getProductPricing().getSellingPrice())));
            } else {
                inv.setAmount(BigDecimal.ZERO);
            }

            inv.setStatus(InventoryStatus.IN_PROGRESS);
        }

        List<WellInventory> all = wellInventoryRepo.findByBarIdAndSessionSessionIdAndWellWellId(barId, sessionId, wellId);
        all.forEach(i -> i.setStatus(InventoryStatus.COMPLETED));
    }

    // ✅ FIX: Aligned to findByBarIdAndSessionSessionId
    public boolean isSessionCompleted(Long barId, Long sessionId) {
        List<WellInventory> all = wellInventoryRepo.findByBarIdAndSessionSessionId(barId, sessionId);
        return !all.isEmpty() && all.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);
    }

    /*
     * -----------------------------------------
     * WELL STATUS MATRIX
     * -----------------------------------------
     */
    public Map<Long, InventoryStatus> getWellStatuses(Long barId, Long sessionId) {
    	// ✅ FIX: Swapped to findByBarId across all loop lookups
    	List<Well> wells = wellRepo.findByBarId(barId);
        // ✅ FIX: Aligned to findByBarIdAndSessionSessionId
        List<WellInventory> all = wellInventoryRepo.findByBarIdAndSessionSessionId(barId, sessionId);

        Map<Long, List<WellInventory>> grouped = all.stream()
                .collect(Collectors.groupingBy(i -> i.getWell().getWellId()));

        Map<Long, InventoryStatus> result = new HashMap<>();

        for (Well w : wells) {
            List<WellInventory> inv = grouped.get(w.getWellId());

            if (inv == null || inv.isEmpty()) {
                result.put(w.getWellId(), InventoryStatus.IN_PROGRESS);
            } else {
                boolean completed = inv.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);
                result.put(w.getWellId(), completed ? InventoryStatus.COMPLETED : InventoryStatus.IN_PROGRESS);
            }
        }
        return result;
    }

    public Long getNextPendingWell(Long barId, Long sessionId) {
    	// ✅ FIX: Swapped to findByBarId across all loop lookups
    	List<Well> wells = wellRepo.findByBarId(barId);

        for (Well w : wells) {
            // ✅ FIX: Aligned to findByBarIdAndSessionSessionIdAndWellWellId
            List<WellInventory> inv = wellInventoryRepo.findByBarIdAndSessionSessionIdAndWellWellId(barId, sessionId, w.getWellId());

            if (inv.isEmpty()) {
                return w.getWellId();
            }

            boolean completed = inv.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);
            if (!completed) {
                return w.getWellId();
            }
        }
        return null;
    }

    public int getSessionProgress(Long barId, Long sessionId) {
    	// ✅ FIX: Swapped to findByBarId across all loop lookups
    	List<Well> wells = wellRepo.findByBarId(barId);
        if (wells.isEmpty()) {
            return 0;
        }

        // ✅ FIX: Aligned to findByBarIdAndSessionSessionId
        List<WellInventory> all = wellInventoryRepo.findByBarIdAndSessionSessionId(barId, sessionId);
        Map<Long, List<WellInventory>> grouped = all.stream()
                .collect(Collectors.groupingBy(i -> i.getWell().getWellId()));

        int completed = 0;
        for (Well w : wells) {
            List<WellInventory> inv = grouped.get(w.getWellId());
            if (inv != null && inv.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED)) {
                completed++;
            }
        }
        return (completed * 100) / wells.size();
    }
}