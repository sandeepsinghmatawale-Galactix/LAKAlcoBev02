package com.barinventory.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.barinventory.dtos.DistributionRequest;
import com.barinventory.entities.Distribution;
import com.barinventory.entities.InventorySession;
import com.barinventory.entities.StockroomInventory;
import com.barinventory.entities.WellDistribution;
import com.barinventory.repos.BrandSizeRepository;
import com.barinventory.repos.DistributionRepository;
import com.barinventory.repos.InventorySessionRepository;
import com.barinventory.repos.StockroomInventoryRepository;
import com.barinventory.repos.WellDistributionRepository;
import com.barinventory.repos.WellRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DistributionService {

	private final DistributionRepository distributionRepo;

	private final WellDistributionRepository wellDistributionRepo;

	private final StockroomInventoryRepository stockroomRepo;

	private final WellRepository wellRepo;

	private final BrandSizeRepository brandSizeRepo;

	private final InventorySessionRepository sessionRepo;

	/*
	 * ----------------------------------------- CREATE DISTRIBUTION
	 * -----------------------------------------
	 */
	public Distribution createDistribution(Long barId, Long sessionId) {

		InventorySession session = sessionRepo.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if (!session.getBar().getBarId().equals(barId)) {

			throw new RuntimeException("Session does not belong to this bar");
		}

		Distribution distribution = new Distribution();

		distribution.setSession(session);

		distribution.setDistributedAt(LocalDateTime.now());

		return distributionRepo.save(distribution);
	}

	/*
	 * ----------------------------------------- MAIN DISTRIBUTION
	 * -----------------------------------------
	 */
	public void distributeStock(Long distributionId, List<DistributionRequest> requests) {

		validateInput(requests);

		Distribution distribution = distributionRepo.findById(distributionId)
				.orElseThrow(() -> new RuntimeException("Distribution not found"));

		Long sessionId = distribution.getSession().getSessionId();

		List<StockroomInventory> stocks = stockroomRepo.findDistributableStocks(sessionId);

		validateAgainstStock(requests, stocks);

		List<WellDistribution> batchList = prepareBatch(requests, distribution);

		wellDistributionRepo.deleteByDistributionId(distributionId);

		wellDistributionRepo.saveAll(batchList);

		wellDistributionRepo.flush();

		validateDistribution(sessionId, distributionId);
	}

	/*
	 * ----------------------------------------- INPUT VALIDATION
	 * -----------------------------------------
	 */
	private void validateInput(List<DistributionRequest> requests) {
	    if (requests == null || requests.isEmpty()) {
	        throw new RuntimeException("No distribution data submitted");
	    }

	    for (DistributionRequest r : requests) {
	        if (r.getDistributedQty() == null || r.getDistributedQty() <= 0) continue;
	        if (r.getBrandSizeId() == null || r.getBrandSizeId() == 0) continue;
	        if (r.getWellId() == null || r.getWellId() == 0) continue;
	        if (r.getDistributedQty() < 0) {
	            throw new RuntimeException("Negative quantity not allowed");
	        }
	    }
	}

	/*
	 * ----------------------------------------- STOCK VALIDATION
	 * -----------------------------------------
	 */
	private void validateAgainstStock(List<DistributionRequest> requests, List<StockroomInventory> stocks) {
		Map<Long, Integer> totalMap = new HashMap<>();

		for (DistributionRequest r : requests) {
			if (r.getDistributedQty() == null || r.getDistributedQty() <= 0)
				continue;
			if (r.getBrandSizeId() == null || r.getBrandSizeId() == 0)
				continue;
			totalMap.merge(r.getBrandSizeId(), r.getDistributedQty(), Integer::sum);
		}

		System.out.println("totalMap: " + totalMap);

		for (StockroomInventory stock : stocks) {
			if (stock.getSaleStock() == null || stock.getSaleStock() == 0)
				continue;

			Long brandSizeId = stock.getBrandSize().getBrandSizeId();
			int actual = totalMap.getOrDefault(brandSizeId, 0);

			System.out
					.println("BrandSizeId=" + brandSizeId + " | Expected=" + stock.getSaleStock() + " | Got=" + actual);

			if (actual != stock.getSaleStock()) {
				throw new RuntimeException("Distribution mismatch for " + stock.getBrandSize().getBrand().getBrandName()
						+ " " + stock.getBrandSize().getSizeMl() + "ml" + " | Expected=" + stock.getSaleStock()
						+ " | Got=" + actual);
			}
		}
	}

	/*
	 * ----------------------------------------- PREPARE BATCH
	 * -----------------------------------------
	 */
	private List<WellDistribution> prepareBatch(List<DistributionRequest> requests, Distribution distribution) {

		List<WellDistribution> list = new ArrayList<>();

		for (DistributionRequest r : requests) {

			if (r.getDistributedQty() == null || r.getDistributedQty() <= 0) {

				continue;
			}

			if (r.getBrandSizeId() == null || r.getWellId() == null) {

				continue;
			}

			WellDistribution wd = new WellDistribution();

			wd.setDistribution(distribution);

			wd.setBrandSize(brandSizeRepo.getReferenceById(r.getBrandSizeId()));

			wd.setWell(wellRepo.getReferenceById(r.getWellId()));

			wd.setDistributedQty(r.getDistributedQty());

			wd.setDistributedAt(LocalDateTime.now());

			list.add(wd);
		}

		return list;
	}

	/*
	 * ----------------------------------------- FINAL VALIDATION
	 * -----------------------------------------
	 */
	private void validateDistribution(Long sessionId, Long distributionId) {

		List<StockroomInventory> stocks = stockroomRepo.findDistributableStocks(sessionId);

		for (StockroomInventory stock : stocks) {

			if (stock.getSaleStock() == null || stock.getSaleStock() == 0) {

				continue;
			}

			Integer distributedQty = wellDistributionRepo.getTotalDistributedQty(distributionId,
					stock.getBrandSize().getBrandSizeId());

			if (distributedQty == null) {
				distributedQty = 0;
			}

			if (!distributedQty.equals(stock.getSaleStock())) {

				throw new RuntimeException(
						"Final validation failed for " + stock.getBrandSize().getBrand().getBrandName() + " "
								+ stock.getBrandSize().getSizeMl() + "ml");
			}
		}
	}

	/*
	 * ----------------------------------------- GET SESSION ID
	 * -----------------------------------------
	 */
	public Long getSessionIdByDistribution(Long distributionId) {

		return distributionRepo.findById(distributionId).map(d -> d.getSession().getSessionId())
				.orElseThrow(() -> new RuntimeException("Distribution not found"));
	}
}