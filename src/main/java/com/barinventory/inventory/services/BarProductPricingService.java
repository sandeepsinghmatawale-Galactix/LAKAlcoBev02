package com.barinventory.inventory.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.CatalogItemResponse;
import com.barinventory.admin.enums.BarPricingStatus;
import com.barinventory.admin.exceptions.ResourceNotFoundException;
import com.barinventory.admin.services.DepotCatalogService;
import com.barinventory.config.SecurityUtils;
import com.barinventory.inventory.dtos.BarProductPricingRequest;
import com.barinventory.inventory.dtos.BarProductPricingResponse;
import com.barinventory.inventory.dtos.UpdateBarPricingRequest;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.repos.BarProductPricingRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BarProductPricingService {
	private final BarProductPricingRepository pricingRepository;
	private final DepotCatalogService catalogService;

	/**
	 * Bar picks a SKU from the admin catalog -> creates their own pricing row with
	 * cached MRP.
	 */
	@Transactional
	public BarProductPricingResponse addPackToBar(BarProductPricingRequest req) {

	    pricingRepository
	            .findByBarIdAndDepotPackId(req.barId(), req.depotPackId())
	            .ifPresent(p -> {
	                throw new IllegalArgumentException(
	                        "Pack already added for this bar.");
	            });

	    CatalogItemResponse item =
	            catalogService.getCatalogItemByPackId(req.depotPackId());

	    // Purchase Price Validation
	    if (req.purchasePrice() == null || req.purchasePrice() <= 0) {
	        throw new IllegalArgumentException(
	                "Purchase price must be greater than zero.");
	    }

	    // Selling Price Validation
	    if (req.sellingPrice() != null
	            && req.sellingPrice() < req.purchasePrice()) {

	        throw new IllegalArgumentException(
	                "Selling price cannot be lower than purchase price.");
	    }

	    BarProductPricing pricing = new BarProductPricing();

	    pricing.setBarId(req.barId());
	    pricing.setDepotBrandId(item.brandId());
	    pricing.setDepotBrandSizeId(item.brandSizeId());
	    pricing.setDepotPackId(item.packId());

	    pricing.setCachedBrandName(item.brandName());
	    pricing.setCachedSizeMl(item.sizeMl());
	    pricing.setCachedPackagingType(item.packagingType());
	    pricing.setCachedMrp(item.mrp());

	    pricing.setPurchasePrice(req.purchasePrice());

	    boolean locked = req.sellingPrice() == null;

	    pricing.setPriceLockedToMrp(locked);

	    pricing.setSellingPrice(
	            locked
	                    ? item.mrp()
	                    : req.sellingPrice()
	    );

	    pricing.setStatus(BarPricingStatus.ACTIVE);

	    pricing.setCreatedAt(LocalDateTime.now());
	    pricing.setUpdatedAt(LocalDateTime.now());

	    return toResponse(pricingRepository.save(pricing));
	}

	@Transactional
	public BarProductPricingResponse update(Long id, UpdateBarPricingRequest req) {

	    BarProductPricing pricing = getEntity(id);

	    if (req.purchasePrice() != null) {
	        pricing.setPurchasePrice(req.purchasePrice());
	    }

	    if (req.priceLockedToMrp() != null) {
	        pricing.setPriceLockedToMrp(req.priceLockedToMrp());
	    }

	    if (req.sellingPrice() != null) {

	        Double effectivePurchasePrice = pricing.getPurchasePrice();

	        if (effectivePurchasePrice != null && req.sellingPrice() < effectivePurchasePrice) {
	            throw new IllegalArgumentException("Selling price cannot be lower than purchase price.");
	        }

	        pricing.setSellingPrice(req.sellingPrice());
	        pricing.setPriceLockedToMrp(false);
	    }

	    if (req.status() != null) {
	        pricing.setStatus(BarPricingStatus.valueOf(req.status()));
	    }

	    pricing.setUpdatedAt(LocalDateTime.now());

	    return toResponse(pricingRepository.save(pricing));
	}

	public List<BarProductPricingResponse> getByBar(Long barId) {
		return pricingRepository.findByBarId(barId).stream().map(this::toResponse).toList();
	}

	public BarProductPricingResponse getById(Long id) {
		return toResponse(getEntity(id));
	}

	/** Called by PackPriceChangeListener on every govt price revision. */
	@Transactional
	public void syncMrpForPack(Long depotPackId, Double newMrp) {
		List<BarProductPricing> rows = pricingRepository.findByDepotPackId(depotPackId);
		for (BarProductPricing row : rows) {
			row.setCachedMrp(newMrp);
			if (Boolean.TRUE.equals(row.getPriceLockedToMrp())) {
				row.setSellingPrice(newMrp);
			}
			row.setUpdatedAt(LocalDateTime.now());
		}
		pricingRepository.saveAll(rows);
	}

	private BarProductPricing getEntity(Long id) {
		return pricingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("BarProductPricing not found: " + id));
	}

	private BarProductPricingResponse toResponse(BarProductPricing p) {
		return new BarProductPricingResponse(p.getId(), p.getBarId(), p.getDepotBrandId(), p.getDepotBrandSizeId(),
				p.getDepotPackId(), p.getCachedBrandName(), p.getCachedSizeMl(), p.getCachedPackagingType(),
				p.getCachedMrp(), p.getPurchasePrice(), p.getSellingPrice(), p.getPriceLockedToMrp(),
				p.getStatus().name());
	}

	public List<BarProductPricingResponse> getMyBarPricing() {
		Long barId = SecurityUtils.getBarId();
		return getByBar(barId);
	}

	@Transactional
	public BarProductPricingResponse updateMySellingPrice(Long pricingId, Double sellingPrice) {

		Long barId = SecurityUtils.getBarId();

		if (sellingPrice == null || sellingPrice <= 0) {
			throw new IllegalArgumentException("Selling price must be greater than zero.");
		}

		BarProductPricing pricing = getEntity(pricingId);

		if (!pricing.getBarId().equals(barId)) {
			throw new IllegalArgumentException("You cannot update pricing for another bar.");
		}

		if (pricing.getPurchasePrice() != null && sellingPrice < pricing.getPurchasePrice()) {
			throw new IllegalArgumentException("Selling price cannot be lower than purchase price.");
		}

		pricing.setSellingPrice(sellingPrice);
		pricing.setPriceLockedToMrp(false);
		pricing.setUpdatedAt(LocalDateTime.now());

		return toResponse(pricingRepository.save(pricing));
	}

}