package com.barinventory.admin.controllers;

//admin/controllers/AdminCatalogController.java

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.admin.dtos.CatalogTreeResponse;
import com.barinventory.admin.services.DepotCatalogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/catalog") 
@RequiredArgsConstructor
public class AdminCatalogController {
	private final DepotCatalogService catalogService;

	@GetMapping("/tree")
	public List<CatalogTreeResponse> getTree() {
		return catalogService.getFullCatalogTree();
	}

	@GetMapping("/tree/{brandId}")
	public CatalogTreeResponse getBrandTree(@PathVariable Long brandId) {
		return catalogService.getBrandTree(brandId);
	}
}