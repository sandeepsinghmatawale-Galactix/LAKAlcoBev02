package com.barinventory.admin.controllers;

//admin/controllers/AdminPackController.java

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.admin.dtos.DepotBrandSizePackResponse;
import com.barinventory.admin.dtos.PriceHistoryResponse;
import com.barinventory.admin.dtos.PriceUpdateRequest;
import com.barinventory.admin.dtos.StatusUpdateRequest;
import com.barinventory.admin.entities.DepotPackPriceHistory;
import com.barinventory.admin.exceptions.ResourceNotFoundException;
import com.barinventory.admin.services.DepotBrandSizePackService;
import com.barinventory.admin.services.DepotPackPriceHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/catalog/packs")
@RequiredArgsConstructor
public class AdminPackController {
	private final DepotBrandSizePackService packService;
	private final DepotPackPriceHistoryService priceHistoryService;

	@GetMapping("/{packId}")
	public DepotBrandSizePackResponse getById(@PathVariable Long packId) {
		return packService.getById(packId);
	}

	@PatchMapping("/{packId}/status")
	public DepotBrandSizePackResponse updateStatus(@PathVariable Long packId, @RequestBody StatusUpdateRequest req) {
		return packService.updateStatus(packId, req);
	}

	// --- Price (govt-order driven, mid-day revisions supported) ---

	@PostMapping("/{packId}/price")
	public PriceHistoryResponse updatePrice(@PathVariable Long packId, @RequestBody PriceUpdateRequest req) {
		return toResponse(priceHistoryService.updatePrice(packId, req));
	}

	@GetMapping("/{packId}/price/current")
	public PriceHistoryResponse getCurrentPrice(@PathVariable Long packId) {
		return priceHistoryService.getCurrentPrice(packId).map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("No active price for pack: " + packId));
	}

	@GetMapping("/{packId}/price/history")
	public List<PriceHistoryResponse> getPriceHistory(@PathVariable Long packId) {
		return priceHistoryService.getHistory(packId).stream().map(this::toResponse).toList();
	}

	private PriceHistoryResponse toResponse(DepotPackPriceHistory p) {
		return new PriceHistoryResponse(p.getPriceId(), p.getPack().getPackId(), p.getMrp(), p.getExciseDuty(),
				p.getBasePrice(), p.getEffectiveFrom(), p.getEffectiveTo(), p.getRevisionReason());
	}
}