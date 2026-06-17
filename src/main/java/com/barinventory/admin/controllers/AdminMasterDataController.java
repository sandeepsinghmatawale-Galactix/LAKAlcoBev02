package com.barinventory.admin.controllers;
//admin/controllers/AdminMasterDataController.java
 

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.admin.dtos.CategoryDto;
import com.barinventory.admin.dtos.DistributorDto;
import com.barinventory.admin.dtos.ManufacturerDto;
import com.barinventory.admin.dtos.SubCategoryDto;
import com.barinventory.admin.services.DepotCategoryService;
import com.barinventory.admin.services.DepotDistributorService;
import com.barinventory.admin.services.DepotManufacturerService;
import com.barinventory.admin.services.DepotSubCategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/catalog/master")
@RequiredArgsConstructor
public class AdminMasterDataController {
 private final DepotCategoryService categoryService;
 private final DepotSubCategoryService subCategoryService;
 private final DepotManufacturerService manufacturerService;
 private final DepotDistributorService distributorService;

 @PostMapping("/categories")
 public CategoryDto createCategory(@RequestBody CategoryDto dto) { return categoryService.create(dto); }

 @PutMapping("/categories/{id}")
 public CategoryDto updateCategory(@PathVariable Long id, @RequestBody CategoryDto dto) { return categoryService.update(id, dto); }

 @GetMapping("/categories")
 public List<CategoryDto> getCategories() { return categoryService.getAll(); }

 @PostMapping("/subcategories")
 public SubCategoryDto createSubCategory(@RequestBody SubCategoryDto dto) { return subCategoryService.create(dto); }

 @GetMapping("/subcategories")
 public List<SubCategoryDto> getSubCategories(@RequestParam(required = false) Long categoryId) {
     return categoryId != null ? subCategoryService.getByCategory(categoryId) : subCategoryService.getAll();
 }

 @PostMapping("/manufacturers")
 public ManufacturerDto createManufacturer(@RequestBody ManufacturerDto dto) { return manufacturerService.create(dto); }

 @PutMapping("/manufacturers/{id}")
 public ManufacturerDto updateManufacturer(@PathVariable Long id, @RequestBody ManufacturerDto dto) { return manufacturerService.update(id, dto); }

 @GetMapping("/manufacturers")
 public List<ManufacturerDto> getManufacturers() { return manufacturerService.getAll(); }

 @PostMapping("/distributors")
 public DistributorDto createDistributor(@RequestBody DistributorDto dto) { return distributorService.create(dto); }

 @PutMapping("/distributors/{id}")
 public DistributorDto updateDistributor(@PathVariable Long id, @RequestBody DistributorDto dto) { return distributorService.update(id, dto); }

 @GetMapping("/distributors")
 public List<DistributorDto> getDistributors() { return distributorService.getAll(); }
}