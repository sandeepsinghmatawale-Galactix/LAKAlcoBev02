package com.barinventory.admin.controllers;

//admin/controllers/AdminBrandController.java
 

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.admin.dtos.DepotBrandRequest;
import com.barinventory.admin.dtos.DepotBrandResponse;
import com.barinventory.admin.dtos.DepotBrandSizeRequest;
import com.barinventory.admin.dtos.DepotBrandSizeResponse;
import com.barinventory.admin.dtos.StatusUpdateRequest;
import com.barinventory.admin.services.DepotBrandService;
import com.barinventory.admin.services.DepotBrandSizeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/catalog/brands") 
@RequiredArgsConstructor
public class AdminBrandController {
 private final DepotBrandService brandService;
 private final DepotBrandSizeService sizeService;

 @PostMapping
 public DepotBrandResponse create(@RequestBody DepotBrandRequest req) { return brandService.create(req); }

 @PutMapping("/{brandId}")
 public DepotBrandResponse update(@PathVariable Long brandId, @RequestBody DepotBrandRequest req) {
     return brandService.update(brandId, req);
 }

 @PatchMapping("/{brandId}/status")
 public DepotBrandResponse updateStatus(@PathVariable Long brandId, @RequestBody StatusUpdateRequest req) {
     return brandService.updateStatus(brandId, req);
 }

 @GetMapping("/{brandId}")
 public DepotBrandResponse getById(@PathVariable Long brandId) { return brandService.getById(brandId); }

 @GetMapping
 public List<DepotBrandResponse> getAll() { return brandService.getAll(); }

 // --- Sizes nested under brand ---

 @PostMapping("/{brandId}/sizes")
 public DepotBrandSizeResponse addSize(@PathVariable Long brandId, @RequestBody DepotBrandSizeRequest req) {
     return sizeService.create(brandId, req);
 }

 @GetMapping("/{brandId}/sizes")
 public List<DepotBrandSizeResponse> getSizes(@PathVariable Long brandId) { return sizeService.getByBrand(brandId); }
}