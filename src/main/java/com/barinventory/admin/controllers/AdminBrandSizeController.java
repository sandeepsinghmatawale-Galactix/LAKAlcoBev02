package com.barinventory.admin.controllers;

//admin/controllers/AdminBrandSizeController.java

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.admin.dtos.DepotBrandSizePackRequest;
import com.barinventory.admin.dtos.DepotBrandSizePackResponse;
import com.barinventory.admin.dtos.DepotBrandSizeResponse;
import com.barinventory.admin.dtos.StatusUpdateRequest;
import com.barinventory.admin.services.DepotBrandSizePackService;
import com.barinventory.admin.services.DepotBrandSizeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/catalog/sizes")
@RequiredArgsConstructor
public class AdminBrandSizeController {
	private final DepotBrandSizeService sizeService;
	private final DepotBrandSizePackService packService;

	@GetMapping("/{brandSizeId}")
	public DepotBrandSizeResponse getById(@PathVariable Long brandSizeId) {
		return sizeService.getById(brandSizeId);
	}

	@PatchMapping("/{brandSizeId}/status")
	public DepotBrandSizeResponse updateStatus(@PathVariable Long brandSizeId, @RequestBody StatusUpdateRequest req) {
		return sizeService.updateStatus(brandSizeId, req);
	}

	// --- Packs (packaging variants) nested under size ---

	@PostMapping("/{brandSizeId}/packs")
	public DepotBrandSizePackResponse addPack(@PathVariable Long brandSizeId,
			@RequestBody DepotBrandSizePackRequest req) {
		return packService.create(brandSizeId, req);
	}

	@GetMapping("/{brandSizeId}/packs")
	public List<DepotBrandSizePackResponse> getPacks(@PathVariable Long brandSizeId) {
		return packService.getByBrandSize(brandSizeId);
	}
}