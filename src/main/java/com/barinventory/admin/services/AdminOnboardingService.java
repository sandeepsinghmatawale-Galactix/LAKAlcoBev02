package com.barinventory.admin.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.admin.dtos.OpeningStockEntry;
import com.barinventory.admin.dtos.WellOpeningStockEntry;
import com.barinventory.admin.enums.BarStatus;
import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.entities.BarUser;
import com.barinventory.auth.enums.Role;
import com.barinventory.auth.repos.BarRepository;
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

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
//admin/services/AdminOnboardingService.java

@Service
@RequiredArgsConstructor
public class AdminOnboardingService {
 private final EntityManager entityManager;
 private final PasswordEncoder passwordEncoder;
 private final BarRepository barRepository;
 private final WellRepository wellRepository;
 private final StockBatchRepository stockBatchRepository;
 private final StockroomInventoryRepository stockroomInventoryRepository;
 private final WellInventoryRepository wellInventoryRepository;
 private final BarProductPricingRepository barProductPricingRepository;
 private final SubscriptionRepository subscriptionRepository;

 @Transactional
 public Long onboardNewStore(OnboardStoreRequest req) {
     // 1. Create Bar
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
     bar.setCreatedAt(LocalDateTime.now());
     bar.setUpdatedAt(LocalDateTime.now());
     bar = barRepository.save(bar);
     Long barId = bar.getBarId();

     // 2. Create BarUser
     BarUser user = new BarUser();
     user.setUsername(req.getAdminUsername());
     user.setPassword(passwordEncoder.encode(req.getAdminPassword()));
     user.setBarId(barId);
     user.setRole(Role.BUSINESS_OWNER);
     entityManager.persist(user);

     // 3. Subscription
     Subscription sub = new Subscription();
     sub.setBarId(barId);
     sub.setStatus(SubscriptionStatus.SUBSCRIBED);
     sub.setTrialType(req.getTrialType());
     if (req.getTrialType() == TrialType.CUSTOM) {
         sub.setStartDate(req.getCustomStartDate());
         sub.setEndDate(req.getCustomEndDate());
     } else {
         sub.setStartDate(LocalDateTime.now());
         sub.setEndDate(calculateExpiry(req.getTrialType()));
     }
     subscriptionRepository.save(sub);

     // 4. Create Wells
     Map<String, Long> wellNameToId = new HashMap<>();
     if (req.getWellNames() != null) {
         for (String wellName : req.getWellNames()) {
             Well well = new Well();
             well.setWellName(wellName);
             well.setBarId(barId);
             well = wellRepository.save(well);
             wellNameToId.put(wellName, well.getWellId());
         }
     }

     // 5. Seed Stockroom Opening Stock
     if (req.getStockroomOpeningStock() != null) {
         for (OpeningStockEntry entry : req.getStockroomOpeningStock()) {
             // StockBatch for FIFO cost tracking
             StockBatch batch = new StockBatch();
             batch.setBarId(barId);
             batch.setDepotPackId(entry.depotPackId());
             batch.setQuantityReceived(entry.openingQty());
             batch.setQuantityRemaining(entry.openingQty());
             batch.setPurchasePricePerUnit(entry.purchasePricePerUnit());
             batch.setInvoiceRefNo(entry.invoiceRefNo());
             batch.setReceivedAt(LocalDateTime.now());
             batch.setCreatedAt(LocalDateTime.now());
             stockBatchRepository.save(batch);

             // StockroomInventory for daily session tracking
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

     // 6. Seed Well Opening Stock
     if (req.getWellOpeningStock() != null) {
         for (WellOpeningStockEntry entry : req.getWellOpeningStock()) {
             Long wellId = wellNameToId.get(entry.wellName());
             if (wellId == null) continue;

             BarProductPricing pricing = barProductPricingRepository
                 .findByBarIdAndDepotPackId(barId, entry.depotPackId())
                 .orElse(null);
             if (pricing == null) continue;

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

     return barId;
 }

 private LocalDateTime calculateExpiry(TrialType type) {
     return switch (type) {
         case WEEKLY -> LocalDateTime.now().plusWeeks(1);
         case MONTHLY -> LocalDateTime.now().plusMonths(1);
         case THREE_MONTH -> LocalDateTime.now().plusMonths(3);
         case YEARLY -> LocalDateTime.now().plusYears(1);
         default -> LocalDateTime.now().plusMonths(1);
     };
 }
}