package com.barinventory.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.barinventory.dtos.WellClosingRequest;
import com.barinventory.entities.InventorySession;
import com.barinventory.entities.InventoryStatus;
import com.barinventory.entities.Well;
import com.barinventory.entities.WellDistribution;
import com.barinventory.entities.WellInventory;
import com.barinventory.repos.InventorySessionRepository;
import com.barinventory.repos.WellDistributionRepository;
import com.barinventory.repos.WellInventoryRepository;
import com.barinventory.repos.WellRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WellInventoryService {

	private final WellRepository wellRepo;
	private final WellInventoryRepository wellInventoryRepo;
	private final WellDistributionRepository wellDistributionRepo;
	private final InventorySessionRepository sessionRepo;

	/*
	 * Step 1: Initialize well inventory
	 */
	@Transactional
	public void initializeWellInventory(Long sessionId, Long wellId) {

		System.out.println("Initializing well: " + wellId);

		// ✅ Fetch session & well
		InventorySession session = sessionRepo.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		Well well = wellRepo.findById(wellId).orElseThrow(() -> new RuntimeException("Well not found"));

		// ✅ Fetch existing inventory ONCE
		List<WellInventory> existingInventories = wellInventoryRepo.findBySessionSessionIdAndWellWellId(sessionId,
				wellId);

		// 🔒 If fully completed → block
		boolean alreadyCompleted = !existingInventories.isEmpty()
				&& existingInventories.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);

		if (alreadyCompleted) {
			throw new RuntimeException("Well already completed. Cannot reopen.");
		}

		// ✅ Build existing brand set (IMPORTANT)
		Set<Long> existingBrandIds = existingInventories.stream().map(inv -> inv.getBrand().getBrandId())
				.collect(Collectors.toSet());

		// ✅ Fetch previous + distribution in ONE GO
		List<WellInventory> previousInventory = wellInventoryRepo.getPreviousWellInventory(wellId);

		List<WellDistribution> distributions = wellDistributionRepo.findByWellWellId(wellId);

		// ✅ Precompute received qty map (OPTIMIZATION)
		Map<Long, Integer> receivedMap = distributions.stream().collect(Collectors.groupingBy(
				d -> d.getBrand().getBrandId(), Collectors.summingInt(WellDistribution::getDistributedQty)));

		// ✅ Collect inserts (batch insert later)
		List<WellInventory> toInsert = new ArrayList<>();

		/*
		 * STEP 1: Existing brands from previous inventory
		 */
		for (WellInventory previous : previousInventory) {

			Long brandId = previous.getBrand().getBrandId();

			// 🔥 CRITICAL: skip if already exists (idempotent)
			if (existingBrandIds.contains(brandId)) {
				continue;
			}

			WellInventory inv = new WellInventory();

			inv.setSession(session);
			inv.setWell(well);
			inv.setBrand(previous.getBrand());
			inv.setOpeningStock(previous.getClosingStock());
			inv.setReceivedStock(receivedMap.getOrDefault(brandId, 0));
			inv.setClosingStock(0);
			inv.setSaleStock(0);
			inv.setStatus(InventoryStatus.IN_PROGRESS);

			toInsert.add(inv);
		}

		/*
		 * STEP 2: New brands from distribution
		 */
		for (WellDistribution dist : distributions) {

			Long brandId = dist.getBrand().getBrandId();

			if (existingBrandIds.contains(brandId)) {
				continue;
			}

			// Also skip if already added in step 1
			boolean alreadyQueued = toInsert.stream().anyMatch(i -> i.getBrand().getBrandId().equals(brandId));

			if (alreadyQueued) {
				continue;
			}

			WellInventory inv = new WellInventory();

			inv.setSession(session);
			inv.setWell(well);
			inv.setBrand(dist.getBrand());
			inv.setOpeningStock(0);
			inv.setReceivedStock(receivedMap.getOrDefault(brandId, 0));
			inv.setClosingStock(0);
			inv.setSaleStock(0);
			inv.setStatus(InventoryStatus.IN_PROGRESS);

			toInsert.add(inv);
		}

		// ✅ FINAL SAVE (batch)
		if (!toInsert.isEmpty()) {
			wellInventoryRepo.saveAll(toInsert);
			System.out.println("Inserted rows: " + toInsert.size());
		} else {
			System.out.println("No new rows inserted (already initialized)");
		}
	}

	/*
	 * Step 2: Get well inventory page
	 */
	public List<WellInventory> getWellInventory(Long sessionId, Long wellId) {
		return wellInventoryRepo.findBySessionSessionIdAndWellWellId(sessionId, wellId);
	}

	/*
	 * Step 3: Update closing stock
	 */
	@Transactional
	public void updateWellClosing(Long sessionId, Long wellId, List<WellClosingRequest> requests) {

		// Validate session & well
		sessionRepo.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));

		wellRepo.findById(wellId).orElseThrow(() -> new RuntimeException("Well not found"));

		// 🔹 STEP 1: Update each brand row
		for (WellClosingRequest request : requests) {

			WellInventory inventory = getInventory(sessionId, wellId, request.getBrandId());

			int totalAvailable = inventory.getOpeningStock() + inventory.getReceivedStock();

			if (request.getClosingStock() > totalAvailable) {
				throw new RuntimeException(
						"Closing stock cannot exceed available stock for brandId: " + request.getBrandId());
			}

			inventory.setClosingStock(request.getClosingStock());
			inventory.setSaleStock(totalAvailable - request.getClosingStock());

			// optional: mark IN_PROGRESS while editing
			inventory.setStatus(InventoryStatus.IN_PROGRESS);

			wellInventoryRepo.save(inventory);
		}

		// 🔹 STEP 2: Mark FULL WELL as COMPLETED (after all brands updated)
		List<WellInventory> inventories = wellInventoryRepo.findBySessionSessionIdAndWellWellId(sessionId, wellId);

		inventories.forEach(inv -> inv.setStatus(InventoryStatus.COMPLETED));

		// No need to call save again if within transactional context (JPA dirty
		// checking)
	}

	private WellInventory getInventory(Long sessionId, Long wellId, Long brandId) {
		return wellInventoryRepo.findBySessionSessionIdAndWellWellIdAndBrandBrandId(sessionId, wellId, brandId)
				.orElseThrow(() -> new RuntimeException(
						"Inventory not found for session=" + sessionId + ", well=" + wellId + ", brand=" + brandId));
	}

	public boolean isSessionCompleted(Long sessionId) {

		List<WellInventory> all = wellInventoryRepo.findBySessionSessionId(sessionId);

		if (all.isEmpty()) {
			return false; // ✅ FIX
		}

		return all.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);
	}

	public Map<Long, InventoryStatus> getWellStatuses(Long sessionId) {

		List<Well> allWells = wellRepo.findAll(); // 👈 IMPORTANT
		List<WellInventory> inventories = wellInventoryRepo.findBySessionSessionId(sessionId);

		Map<Long, List<WellInventory>> grouped = inventories.stream()
				.collect(Collectors.groupingBy(i -> i.getWell().getWellId()));

		Map<Long, InventoryStatus> result = new HashMap<>();

		for (Well well : allWells) {

			List<WellInventory> wellInv = grouped.get(well.getWellId());

			if (wellInv == null || wellInv.isEmpty()) {
				result.put(well.getWellId(), InventoryStatus.IN_PROGRESS); // ✅ FIX
			} else {
				boolean completed = wellInv.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);

				result.put(well.getWellId(), completed ? InventoryStatus.COMPLETED : InventoryStatus.IN_PROGRESS);
			}
		}

		return result;
	}

	public Long getNextPendingWell(Long sessionId) {

		List<Well> allWells = wellRepo.findAll(); // ✅ ALL wells

		for (Well well : allWells) {

			List<WellInventory> inventories = wellInventoryRepo.findBySessionSessionIdAndWellWellId(sessionId,
					well.getWellId());

			// ✅ NOT STARTED
			if (inventories.isEmpty()) {
				return well.getWellId();
			}

			// ✅ CHECK COMPLETED
			boolean completed = inventories.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);

			if (!completed) {
				return well.getWellId();
			}
		}

		return null; // all completed
	}

	public boolean isWellCompleted(Long sessionId, Long wellId) {

		List<WellInventory> inventories = wellInventoryRepo.findBySessionSessionIdAndWellWellId(sessionId, wellId);

		return !inventories.isEmpty()
				&& inventories.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);
	}

	public int getSessionProgress(Long sessionId) {

		List<Well> allWells = wellRepo.findAll();

		if (allWells.isEmpty()) {
			return 0;
		}

		List<WellInventory> allInventories = wellInventoryRepo.findBySessionSessionId(sessionId);

		Map<Long, List<WellInventory>> grouped = allInventories.stream()
				.collect(Collectors.groupingBy(i -> i.getWell().getWellId()));

		int completedCount = 0;

		for (Well well : allWells) {

			List<WellInventory> inventories = grouped.get(well.getWellId());

			boolean completed = inventories != null
					&& inventories.stream().allMatch(inv -> inv.getStatus() == InventoryStatus.COMPLETED);

			if (completed) {
				completedCount++;
			}
		}

		return (completedCount * 100) / allWells.size();
	}

}