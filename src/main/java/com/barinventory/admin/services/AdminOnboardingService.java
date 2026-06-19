package com.barinventory.admin.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.admin.dtos.OpeningStockEntry;
import com.barinventory.admin.dtos.WellOpeningStockEntry;
import com.barinventory.admin.enums.BarRole;
import com.barinventory.admin.enums.BarStatus;
import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.entities.BarUser;
import com.barinventory.auth.entities.UserBarAccess;
import com.barinventory.auth.enums.Role;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.auth.repos.BarUserRepository;
import com.barinventory.auth.repos.UserBarAccessRepository;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.entities.StockBatch;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.entities.Well;
import com.barinventory.inventory.entities.WellInventory;
import com.barinventory.inventory.repos.BarProductPricingRepository;
import com.barinventory.inventory.repos.StockBatchRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;
import com.barinventory.inventory.repos.WellInventoryRepository;
import com.barinventory.inventory.repos.WellRepository;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.enums.TrialType;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOnboardingService {

	private final PasswordEncoder passwordEncoder;
	private final BarRepository barRepository;
	private final BarUserRepository barUserRepository;
	private final UserBarAccessRepository userBarAccessRepository;
	private final WellRepository wellRepository;
	private final StockBatchRepository stockBatchRepository;
	private final StockroomInventoryRepository stockroomInventoryRepository;
	private final WellInventoryRepository wellInventoryRepository;
	private final BarProductPricingRepository barProductPricingRepository;
	private final SubscriptionRepository subscriptionRepository;

	@Transactional
	public Long onboardNewStore(OnboardStoreRequest req) {

		LocalDateTime now = LocalDateTime.now();

		Bar bar = new Bar();
		bar.setBarName(req.getStoreName());
		bar.setOwnerName(req.getOwnerName());
		bar.setPhone(req.getPhone());
		bar.setEmail(req.getEmail());
		bar.setLicenseNumber(req.getLicenseNumber());
		bar.setAddress(req.getAddress());
		bar.setCity(req.getCity());
		bar.setState(req.getState());
		bar.setPincode(req.getPincode());
		bar.setStatus(BarStatus.ACTIVE);
		bar.setCreatedAt(now);
		bar.setUpdatedAt(now);

		bar = barRepository.save(bar);
		Long barId = bar.getBarId();

		BarUser user = new BarUser();
		user.setUsername(req.getAdminUsername());
		user.setPassword(passwordEncoder.encode(req.getAdminPassword()));
		user.setBarId(barId); // keep for current app compatibility
		user.setRole(Role.BUSINESS_OWNER);

		user = barUserRepository.save(user);

		createUserBarAccessIfMissing(user.getId(), barId, BarRole.BAR_OWNER);

		Subscription sub = new Subscription();
		sub.setBarId(barId);
		sub.setStatus(SubscriptionStatus.SUBSCRIBED);
		sub.setTrialType(req.getTrialType());

		if (req.getTrialType() == TrialType.CUSTOM) {
			sub.setStartDate(req.getCustomStartDate());
			sub.setEndDate(req.getCustomEndDate());
		} else {
			sub.setStartDate(now);
			sub.setEndDate(calculateExpiry(req.getTrialType(), now));
		}

		subscriptionRepository.save(sub);

		Map<String, Long> wellNameToId = createWellsForBar(barId, req);

		seedStockroomOpeningStock(barId, req);

		seedWellOpeningStock(barId, req, wellNameToId);

		return barId;
	}

	private void createUserBarAccessIfMissing(Long userId, Long barId, BarRole barRole) {

		if (userBarAccessRepository.existsByUserIdAndBarId(userId, barId)) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();

		UserBarAccess access = new UserBarAccess();

		access.setUserId(userId);
		access.setBarId(barId);
		access.setBarRole(barRole);
		access.setActive(true);

		// TODO:
		// Later replace with logged-in admin id
		access.setGrantedBy(1L);

		access.setCreatedAt(now);
		access.setUpdatedAt(now);

		userBarAccessRepository.save(access);
	}

	private Map<String, Long> createWellsForBar(Long barId, OnboardStoreRequest req) {
		Map<String, Long> wellNameToId = new HashMap<>();

		if (req.getWellNames() == null) {
			return wellNameToId;
		}

		for (String wellName : req.getWellNames()) {
			if (wellName == null || wellName.isBlank()) {
				continue;
			}

			String cleanedWellName = wellName.trim();

			Optional<Well> existingWell = wellRepository.findByBarIdAndWellNameIgnoreCase(barId, cleanedWellName);

			Well well = existingWell.orElseGet(() -> {
				Well newWell = new Well();
				newWell.setWellName(cleanedWellName);
				newWell.setBarId(barId);
				newWell.setActive(true);
				return wellRepository.save(newWell);
			});

			if (Boolean.FALSE.equals(well.getActive())) {
				well.setActive(true);
				well = wellRepository.save(well);
			}

			wellNameToId.put(cleanedWellName, well.getWellId());
		}

		return wellNameToId;
	}

	private void seedStockroomOpeningStock(Long barId, OnboardStoreRequest req) {
		if (req.getStockroomOpeningStock() == null) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();

		for (OpeningStockEntry entry : req.getStockroomOpeningStock()) {
			StockBatch batch = new StockBatch();
			batch.setBarId(barId);
			batch.setDepotPackId(entry.depotPackId());
			batch.setQuantityReceived(entry.openingQty());
			batch.setQuantityRemaining(entry.openingQty());
			batch.setPurchasePricePerUnit(entry.purchasePricePerUnit());
			batch.setInvoiceRefNo(entry.invoiceRefNo());
			batch.setReceivedAt(now);
			batch.setCreatedAt(now);

			stockBatchRepository.save(batch);

			StockroomInventory stockroom = new StockroomInventory();
			stockroom.setBarId(barId);
			stockroom.setDepotBrandSizeId(entry.depotBrandSizeId());
			stockroom.setOpeningStock(entry.openingQty());
			stockroom.setReceivedStock(0);
			stockroom.setClosingStock(entry.openingQty());
			stockroom.setSaleStock(0);

			stockroomInventoryRepository.save(stockroom);
		}
	}

	private void seedWellOpeningStock(Long barId, OnboardStoreRequest req, Map<String, Long> wellNameToId) {

		if (req.getWellOpeningStock() == null) {
			return;
		}

		for (WellOpeningStockEntry entry : req.getWellOpeningStock()) {
			Long wellId = wellNameToId.get(entry.wellName());

			if (wellId == null) {
				continue;
			}

			BarProductPricing pricing = barProductPricingRepository
					.findByBarIdAndDepotPackId(barId, entry.depotPackId()).orElse(null);

			if (pricing == null) {
				continue;
			}

			WellInventory wellInventory = new WellInventory();
			wellInventory.setBarId(barId);
			wellInventory.setWell(wellRepository.findById(wellId).orElseThrow());
			wellInventory.setProductPricing(pricing);
			wellInventory.setOpeningStock(entry.openingQty());
			wellInventory.setReceivedStock(0);
			wellInventory.setClosingStock(entry.openingQty());
			wellInventory.setSaleStock(0);

			wellInventoryRepository.save(wellInventory);
		}
	}

	private LocalDateTime calculateExpiry(TrialType type, LocalDateTime start) {
		return switch (type) {
		case WEEKLY -> start.plusWeeks(1);
		case MONTHLY -> start.plusMonths(1);
		case THREE_MONTH -> start.plusMonths(3);
		case YEARLY -> start.plusYears(1);
		default -> start.plusMonths(1);
		};
	}
}