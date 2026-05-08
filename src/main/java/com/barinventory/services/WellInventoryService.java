package com.barinventory.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.barinventory.dtos.WellClosingRequest;
import com.barinventory.entities.InventorySession;
import com.barinventory.entities.InventoryStatus;
import com.barinventory.entities.Well;
import com.barinventory.entities.WellDistribution;
import com.barinventory.entities.WellInventory;
import com.barinventory.repos.InventorySessionRepository;
import com.barinventory.repos.WellDistributionRepository;
import com.barinventory.repos.WellInventoryRepository;
import com.barinventory.repos.WellRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WellInventoryService {

    private final WellRepository wellRepo;

    private final WellInventoryRepository
            wellInventoryRepo;

    private final WellDistributionRepository
            wellDistributionRepo;

    private final InventorySessionRepository
            sessionRepo;

    /*
     -----------------------------------------
     INITIALIZE WELL INVENTORY
     -----------------------------------------
    */
    @Transactional
    public void initializeWellInventory(
            Long barId,
            Long sessionId,
            Long wellId
    ) {

        InventorySession session =
                sessionRepo
                .findBySessionIdAndBarBarId(
                        sessionId,
                        barId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"
                        ));

        Well well =
                wellRepo.findByWellIdAndBar_BarId(
                        wellId,
                        barId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Well not found"
                        ));

        List<WellInventory> existing =
                wellInventoryRepo
                .lockAndFindByBarSessionWell(
                        barId,
                        sessionId,
                        wellId
                );

        boolean alreadyCompleted =
                !existing.isEmpty()
                &&
                existing.stream()
                .allMatch(i ->
                        i.getStatus()
                        == InventoryStatus.COMPLETED
                );

        if (alreadyCompleted) {

            throw new RuntimeException(
                    "Well already completed"
            );
        }

        if (!existing.isEmpty()) {
            return;
        }

        /*
         -----------------------------------------
         PREVIOUS INVENTORY
         -----------------------------------------
        */

        List<WellInventory> previousInventory =
                wellInventoryRepo
                .getPreviousWellInventory(
                        barId,
                        wellId,
                        sessionId
                );

        /*
         -----------------------------------------
         DISTRIBUTIONS
         -----------------------------------------
        */

        List<WellDistribution> distributions =
                wellDistributionRepo
                .findByWellSessionAndBar(
                        wellId,
                        sessionId,
                        barId
                );

        /*
         -----------------------------------------
         RECEIVED MAP
         brandSizeId -> qty
         -----------------------------------------
        */

        Map<Long, Integer> receivedMap =
                distributions.stream()
                .collect(Collectors.groupingBy(

                        d -> d.getBrandSize()
                        .getBrandSizeId(),

                        Collectors.summingInt(
                                WellDistribution
                                ::getDistributedQty
                        )
                ));

        List<WellInventory> toInsert =
                new ArrayList<>();

        /*
         -----------------------------------------
         PREVIOUS STOCKS
         -----------------------------------------
        */

        for (WellInventory prev : previousInventory) {

            Long brandSizeId =
                    prev.getBrandSize()
                    .getBrandSizeId();

            WellInventory inv =
                    new WellInventory();

            inv.setBar(session.getBar());

            inv.setSession(session);

            inv.setWell(well);

            inv.setBrandSize(
                    prev.getBrandSize()
            );

            inv.setOpeningStock(
                    prev.getClosingStock()
            );

            inv.setReceivedStock(
                    receivedMap.getOrDefault(
                            brandSizeId,
                            0
                    )
            );

            inv.setClosingStock(0);

            inv.setSaleStock(0);

            inv.setStatus(
                    InventoryStatus.IN_PROGRESS
            );

            toInsert.add(inv);
        }

        /*
         -----------------------------------------
         EXISTING IDS
         -----------------------------------------
        */

        Set<Long> existingBrandSizeIds =
                toInsert.stream()

                .map(i ->
                        i.getBrandSize()
                        .getBrandSizeId()
                )

                .collect(Collectors.toSet());

        /*
         -----------------------------------------
         NEW ITEMS FROM DISTRIBUTION
         -----------------------------------------
        */

        for (WellDistribution dist : distributions) {

            Long brandSizeId =
                    dist.getBrandSize()
                    .getBrandSizeId();

            if (existingBrandSizeIds
                    .contains(brandSizeId)) {

                continue;
            }

            WellInventory inv =
                    new WellInventory();

            inv.setBar(session.getBar());

            inv.setSession(session);

            inv.setWell(well);

            inv.setBrandSize(
                    dist.getBrandSize()
            );

            inv.setOpeningStock(0);

            inv.setReceivedStock(
                    receivedMap.getOrDefault(
                            brandSizeId,
                            0
                    )
            );

            inv.setClosingStock(0);

            inv.setSaleStock(0);

            inv.setStatus(
                    InventoryStatus.IN_PROGRESS
            );

            toInsert.add(inv);

            existingBrandSizeIds.add(
                    brandSizeId
            );
        }

        if (!toInsert.isEmpty()) {

            wellInventoryRepo.saveAll(toInsert);
        }
    }

    /*
     -----------------------------------------
     GET WELL INVENTORY
     -----------------------------------------
    */
    public List<WellInventory>
    getWellInventory(
            Long barId,
            Long sessionId,
            Long wellId
    ) {

        return wellInventoryRepo
                .findByBarBarIdAndSessionSessionIdAndWellWellId(
                        barId,
                        sessionId,
                        wellId
                );
    }

    /*
     -----------------------------------------
     UPDATE WELL CLOSING
     -----------------------------------------
    */
    public void updateWellClosing(
            Long barId,
            Long sessionId,
            Long wellId,
            List<WellClosingRequest> requests
    ) {

        sessionRepo
                .findBySessionIdAndBarBarId(
                        sessionId,
                        barId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"
                        ));

        wellRepo
                .findByWellIdAndBar_BarId(
                        wellId,
                        barId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Well not found"
                        ));

        for (WellClosingRequest req : requests) {

            WellInventory inv =
                    wellInventoryRepo
                    .findByBarBarIdAndSessionSessionIdAndWellWellIdAndBrandSizeBrandSizeId(
                            barId,
                            sessionId,
                            wellId,
                            req.getBrandSizeId()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Inventory not found"
                            ));

            int total =
                    inv.getOpeningStock()
                    + inv.getReceivedStock();

            if (req.getClosingStock() > total) {

                throw new RuntimeException(
                        "Invalid closing stock"
                );
            }

            inv.setClosingStock(
                    req.getClosingStock()
            );

            inv.setSaleStock(
                    total - req.getClosingStock()
            );

            inv.setStatus(
                    InventoryStatus.IN_PROGRESS
            );
        }

        List<WellInventory> all =
                wellInventoryRepo
                .findByBarBarIdAndSessionSessionIdAndWellWellId(
                        barId,
                        sessionId,
                        wellId
                );

        all.forEach(i ->
                i.setStatus(
                        InventoryStatus.COMPLETED
                )
        );
    }

    /*
     -----------------------------------------
     SESSION COMPLETED
     -----------------------------------------
    */
    public boolean isSessionCompleted(
            Long barId,
            Long sessionId
    ) {

        List<WellInventory> all =
                wellInventoryRepo
                .findByBarBarIdAndSessionSessionId(
                        barId,
                        sessionId
                );

        return !all.isEmpty()
                &&
                all.stream()
                .allMatch(i ->
                        i.getStatus()
                        == InventoryStatus.COMPLETED
                );
    }

    /*
     -----------------------------------------
     WELL STATUS MAP
     -----------------------------------------
    */
    public Map<Long, InventoryStatus>
    getWellStatuses(
            Long barId,
            Long sessionId
    ) {

        List<Well> wells =
                wellRepo.findByBar_BarId(barId);

        List<WellInventory> all =
                wellInventoryRepo
                .findByBarBarIdAndSessionSessionId(
                        barId,
                        sessionId
                );

        Map<Long, List<WellInventory>> grouped =
                all.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getWell().getWellId()
                ));

        Map<Long, InventoryStatus> result =
                new HashMap<>();

        for (Well w : wells) {

            List<WellInventory> inv =
                    grouped.get(w.getWellId());

            if (inv == null || inv.isEmpty()) {

                result.put(
                        w.getWellId(),
                        InventoryStatus.IN_PROGRESS
                );

            } else {

                boolean completed =
                        inv.stream()
                        .allMatch(i ->
                                i.getStatus()
                                == InventoryStatus.COMPLETED
                        );

                result.put(
                        w.getWellId(),

                        completed
                        ? InventoryStatus.COMPLETED
                        : InventoryStatus.IN_PROGRESS
                );
            }
        }

        return result;
    }

    /*
     -----------------------------------------
     NEXT PENDING WELL
     -----------------------------------------
    */
    public Long getNextPendingWell(
            Long barId,
            Long sessionId
    ) {

        List<Well> wells =
                wellRepo.findByBar_BarId(barId);

        for (Well w : wells) {

            List<WellInventory> inv =
                    wellInventoryRepo
                    .findByBarBarIdAndSessionSessionIdAndWellWellId(
                            barId,
                            sessionId,
                            w.getWellId()
                    );

            if (inv.isEmpty()) {
                return w.getWellId();
            }

            boolean completed =
                    inv.stream()
                    .allMatch(i ->
                            i.getStatus()
                            == InventoryStatus.COMPLETED
                    );

            if (!completed) {
                return w.getWellId();
            }
        }

        return null;
    }

    /*
     -----------------------------------------
     SESSION PROGRESS
     -----------------------------------------
    */
    public int getSessionProgress(
            Long barId,
            Long sessionId
    ) {

        List<Well> wells =
                wellRepo.findByBar_BarId(barId);

        if (wells.isEmpty()) {
            return 0;
        }

        List<WellInventory> all =
                wellInventoryRepo
                .findByBarBarIdAndSessionSessionId(
                        barId,
                        sessionId
                );

        Map<Long, List<WellInventory>> grouped =
                all.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getWell().getWellId()
                ));

        int completed = 0;

        for (Well w : wells) {

            List<WellInventory> inv =
                    grouped.get(w.getWellId());

            if (inv != null
                    &&
                    inv.stream()
                    .allMatch(i ->
                            i.getStatus()
                            == InventoryStatus.COMPLETED
                    )) {

                completed++;
            }
        }

        return (completed * 100)
                / wells.size();
    }
}