package com.barinventory.auth.servcies;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barinventory.auth.entities.Brand;
import com.barinventory.auth.repos.BrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {

	private final BrandRepository brandRepository;

	public List<Brand> getAllBrands() {
		return brandRepository.findAll();
	}

	public Brand getBrandById(Long brandId) {
		return brandRepository.findById(brandId).orElseThrow(() -> new RuntimeException("Brand not found"));
	}

	// ✅ add this — called from DistributionController
	public List<Brand> getBrandsByBar(Long barId) {
		return brandRepository.findByBarBarId(barId);
	}
}