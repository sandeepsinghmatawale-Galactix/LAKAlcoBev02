package com.barinventory.admin.services;

// admin/services/AdminOpeningStockService.java

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barinventory.admin.dtos.AdminOpeningStockRequest;
import com.barinventory.admin.dtos.AdminOpeningStockResponse;
import com.barinventory.admin.dtos.BarWellsResponse;
import com.barinventory.admin.dtos.StockroomOpeningLine;
import com.barinventory.admin.dtos.WellOpeningLine;
import com.barinventory.admin.enums.BarPricingStatus;
import com.barinventory.admin.exceptions.ResourceNotFoundException;
import com.barinventory.admin.repsitory.DepotBrandSizePackRepository;
import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.entities.InventoryStatus;
import com.barinventory.inventory.entities.StockBatch;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.entities.Well;
import com.barinventory.inventory.entities.WellInventory;
import com.barinventory.inventory.repos.BarProductPricingRepository;
import com.barinventory.inventory.repos.StockBatchRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;
import com.barinventory.inventory.repos.WellInventoryRepository;
import com.barinventory.inventory.repos.WellRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOpeningStockService {

    private final BarRepository barRepository;
    private final WellRepository wellRepo;
    private final BarProductPricingRepository pricingRepo;
    private final StockroomInventoryRepository stockroomRepo;
    private final WellInventoryRepository wellInventoryRepo;
    private final StockBatchRepository stockBatchRepo;
    private final DepotBrandSizePackRepository packRepository;

    public BarWellsResponse getBarWells(Long barId) {

        Bar bar = barRepository.findById(barId)
                .orElseThrow(() -> new ResourceNotFoundException("Bar not found: " + barId));

        List<BarWellsResponse.WellDto> wells = wellRepo.findByBarId(barId)
                .stream()
                .map(w -> new BarWellsResponse.WellDto(w.getWellId(), w.getWellName()))
                .toList();

        return new BarWellsResponse(barId, bar.getBarName(), wells);
    }

    @Transactional
    public AdminOpeningStockResponse seedOpeningStock(AdminOpeningStockRequest req) {

        Bar bar = barRepository.findById(req.barId())
                .orElseThrow(() -> new ResourceNotFoundException("Bar not found: " + req.barId()));

        List<Well> barWells = wellRepo.findByBarId(req.barId());

        Map<Long, Well> wellMap = barWells.stream()
                .collect(Collectors.toMap(Well::getWellId, w -> w));

        if (req.wellLines() != null) {
            for (WellOpeningLine line : req.wellLines()) {
                if (!wellMap.containsKey(line.wellId())) {
                    throw new IllegalArgumentException(
                            "Well " + line.wellId() + " does not belong to bar " + req.barId()
                    );
                }
            }
        }

        int stockroomCount = 0;

        if (req.stockroomLines() != null) {
            for (StockroomOpeningLine line : req.stockroomLines()) {

                BarProductPricing pricing = pricingRepo
                        .findByBarIdAndDepotBrandSizeId(req.barId(), line.depotBrandSizeId())
                        .orElseGet(() -> {
                            BarProductPricing p = new BarProductPricing();

                            p.setBarId(req.barId());
                            p.setDepotBrandSizeId(line.depotBrandSizeId());
                            p.setDepotPackId(line.depotPackId());

                            p.setDepotBrandId(
                                    packRepository.findById(line.depotPackId())
                                            .map(pack -> pack.getBrandSize().getBrand().getBrandId())
                                            .orElseThrow(() -> new ResourceNotFoundException(
                                                    "Pack not found: " + line.depotPackId()
                                            ))
                            );

                            p.setCachedBrandName(line.brandName());
                            p.setCachedSizeMl(line.sizeMl());
                            p.setCachedPackagingType(line.packagingType());
                            p.setCachedMrp(line.mrp());

                            p.setPurchasePrice(line.purchasePricePerUnit());
                            p.setSellingPrice(line.sellingPrice() != null ? line.sellingPrice() : line.mrp());
                            p.setPriceLockedToMrp(line.sellingPrice() == null);

                            p.setStatus(BarPricingStatus.ACTIVE);
                            p.setCreatedAt(LocalDateTime.now());
                            p.setUpdatedAt(LocalDateTime.now());

                            return pricingRepo.save(p);
                        });

                pricing.setDepotPackId(line.depotPackId());
                pricing.setCachedBrandName(line.brandName());
                pricing.setCachedSizeMl(line.sizeMl());
                pricing.setCachedPackagingType(line.packagingType());
                pricing.setCachedMrp(line.mrp());
                pricing.setPurchasePrice(line.purchasePricePerUnit());
                pricing.setSellingPrice(line.sellingPrice() != null ? line.sellingPrice() : line.mrp());
                pricing.setPriceLockedToMrp(line.sellingPrice() == null);
                pricing.setStatus(BarPricingStatus.ACTIVE);
                pricing.setUpdatedAt(LocalDateTime.now());

                pricingRepo.save(pricing);

                boolean batchExists = stockBatchRepo
                        .findByBarIdAndDepotPackId(req.barId(), line.depotPackId())
                        .isPresent();

                if (!batchExists) {
                    StockBatch batch = new StockBatch();

                    batch.setBarId(req.barId());
                    batch.setDepotPackId(line.depotPackId());
                    batch.setQuantityReceived(line.openingQty());
                    batch.setQuantityRemaining(line.openingQty());
                    batch.setPurchasePricePerUnit(line.purchasePricePerUnit());
                    batch.setInvoiceRefNo("OPENING-STOCK");
                    batch.setReceivedAt(LocalDateTime.now());
                    batch.setCreatedAt(LocalDateTime.now());

                    stockBatchRepo.save(batch);
                }

                stockroomRepo
                        .findByBarIdAndDepotBrandSizeIdAndSessionIsNull(req.barId(), line.depotBrandSizeId())
                        .ifPresentOrElse(existing -> {
                            existing.setOpeningStock(line.openingQty());
                            existing.setClosingStock(line.openingQty());
                            existing.setReceivedStock(0);
                            existing.setSaleStock(0);
                            stockroomRepo.save(existing);
                        }, () -> {
                            StockroomInventory stockroom = new StockroomInventory();

                            stockroom.setBarId(req.barId());
                            stockroom.setDepotBrandSizeId(line.depotBrandSizeId());
                            stockroom.setSession(null);
                            stockroom.setOpeningStock(line.openingQty());
                            stockroom.setReceivedStock(0);
                            stockroom.setClosingStock(line.openingQty());
                            stockroom.setSaleStock(0);

                            stockroomRepo.save(stockroom);
                        });

                stockroomCount++;
            }
        }

        int wellLinesCount = 0;
        Map<Long, Integer> wellItemCount = new java.util.HashMap<>();

        if (req.wellLines() != null) {
            for (WellOpeningLine line : req.wellLines()) {

                Well well = wellMap.get(line.wellId());

                BarProductPricing pricing = pricingRepo
                        .findByBarIdAndDepotBrandSizeId(req.barId(), line.depotBrandSizeId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No pricing found for packId " + line.depotPackId()
                                        + ". Add this SKU to stockroom opening stock first."
                        ));

                wellInventoryRepo
                        .findByBarIdAndWellWellIdAndProductPricingIdAndSessionIsNull(
                                req.barId(),
                                line.wellId(),
                                pricing.getId()
                        )
                        .ifPresentOrElse(existing -> {
                            existing.setOpeningStock(line.openingQty());
                            existing.setClosingStock(line.openingQty());
                            existing.setReceivedStock(0);
                            existing.setSaleStock(0);
                            existing.setAmount(BigDecimal.ZERO);
                            existing.setStatus(InventoryStatus.IN_PROGRESS);
                            wellInventoryRepo.save(existing);
                        }, () -> {
                            WellInventory wi = new WellInventory();

                            wi.setBarId(req.barId());
                            wi.setWell(well);
                            wi.setProductPricing(pricing);
                            wi.setSession(null);
                            wi.setOpeningStock(line.openingQty());
                            wi.setReceivedStock(0);
                            wi.setClosingStock(line.openingQty());
                            wi.setSaleStock(0);
                            wi.setAmount(BigDecimal.ZERO);
                            wi.setStatus(InventoryStatus.IN_PROGRESS);

                            wellInventoryRepo.save(wi);
                        });

                wellItemCount.merge(line.wellId(), 1, Integer::sum);
                wellLinesCount++;
            }
        }

        List<AdminOpeningStockResponse.WellSummary> wellSummaries =
                wellItemCount.entrySet()
                        .stream()
                        .map(e -> new AdminOpeningStockResponse.WellSummary(
                                e.getKey(),
                                wellMap.get(e.getKey()).getWellName(),
                                e.getValue()
                        ))
                        .toList();

        return new AdminOpeningStockResponse(
                req.barId(),
                bar.getBarName(),
                stockroomCount,
                wellLinesCount,
                wellSummaries
        );
    }
}