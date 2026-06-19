package com.barinventory.inventory.controllers;

//inventory/controllers/BarProductPricingController.java

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.inventory.dtos.BarProductPricingRequest;
import com.barinventory.inventory.dtos.BarProductPricingResponse;
import com.barinventory.inventory.dtos.UpdateBarPricingRequest;
import com.barinventory.inventory.services.BarProductPricingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory/bar-pricing")
@RequiredArgsConstructor
public class BarProductPricingController {
	private final BarProductPricingService pricingService;

	@PostMapping
	public BarProductPricingResponse add(@RequestBody BarProductPricingRequest req) {
		return pricingService.addPackToBar(req);
	}

	@PatchMapping("/{id}")
	public BarProductPricingResponse update(@PathVariable Long id, @RequestBody UpdateBarPricingRequest req) {
		return pricingService.update(id, req);
	}

	@GetMapping("/{id}")
	public BarProductPricingResponse getById(@PathVariable Long id) {
		return pricingService.getById(id);
	}

	@GetMapping
	public List<BarProductPricingResponse> getByBar(@RequestParam Long barId) {
		return pricingService.getByBar(barId);
	}
	
	@GetMapping("/my")
	public List<BarProductPricingResponse> getMyPricing() {
	    return pricingService.getMyBarPricing();
	}
	@PatchMapping("/my/{pricingId}")
	public BarProductPricingResponse updateMySellingPrice(
	        @PathVariable Long pricingId,
	        @RequestParam Double sellingPrice) {

	    return pricingService.updateMySellingPrice(
	            pricingId,
	            sellingPrice);
	}
	
	
	
}