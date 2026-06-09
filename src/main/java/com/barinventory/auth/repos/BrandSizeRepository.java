package com.barinventory.auth.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.auth.entities.BrandSize;

public interface BrandSizeRepository extends JpaRepository<BrandSize, Long> {

	List<BrandSize> findByBarBarId(Long barId);

	List<BrandSize> findByBrandBrandId(Long brandId);

	Optional<BrandSize> findByBrandBrandIdAndSizeMl(Long brandId, Integer sizeMl);
}