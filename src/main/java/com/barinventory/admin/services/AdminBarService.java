package com.barinventory.admin.services;
//admin/services/AdminBarService.java
 

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.BarSummaryResponse;
import com.barinventory.admin.dtos.EditBarRequest;
import com.barinventory.admin.dtos.EditSubscriptionRequest;
import com.barinventory.admin.enums.BarStatus;
import com.barinventory.admin.exceptions.ResourceNotFoundException;
import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.enums.TrialType;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
 

@Service
@RequiredArgsConstructor
public class AdminBarService {
 private final BarRepository barRepository;
 private final SubscriptionRepository subscriptionRepository;

 public List<BarSummaryResponse> getAllBars() {
     return barRepository.findAll().stream().map(bar -> {
         Subscription sub = subscriptionRepository.findByBarId(bar.getBarId()).orElse(null);
         return new BarSummaryResponse(
             bar.getBarId(), bar.getBarName(), bar.getOwnerName(), bar.getPhone(),
             bar.getEmail(), bar.getLicenseNumber(), bar.getCity(), bar.getState(),
             bar.getStatus().name(),
             sub != null ? sub.getStatus().name() : "NONE",
             sub != null ? sub.getEndDate() : null
         );
     }).toList();
 }

 @Transactional
 public BarSummaryResponse editBar(Long barId, EditBarRequest req) {
     Bar bar = barRepository.findById(barId)
         .orElseThrow(() -> new ResourceNotFoundException("Bar not found: " + barId));
     bar.setBarName(req.barName());
     bar.setOwnerName(req.ownerName());
     bar.setPhone(req.phone());
     bar.setEmail(req.email());
     bar.setLicenseNumber(req.licenseNumber());
     bar.setAddress(req.address());
     bar.setCity(req.city());
     bar.setState(req.state());
     bar.setPincode(req.pincode());
     if (req.status() != null) bar.setStatus(BarStatus.valueOf(req.status()));
     bar.setUpdatedAt(LocalDateTime.now());
     bar = barRepository.save(bar);

     Subscription sub = subscriptionRepository.findByBarId(barId).orElse(null);
     return new BarSummaryResponse(
         bar.getBarId(), bar.getBarName(), bar.getOwnerName(), bar.getPhone(),
         bar.getEmail(), bar.getLicenseNumber(), bar.getCity(), bar.getState(),
         bar.getStatus().name(),
         sub != null ? sub.getStatus().name() : "NONE",
         sub != null ? sub.getEndDate() : null
     );
 }

 @Transactional
 public void editSubscription(Long barId, EditSubscriptionRequest req) {
     Subscription sub = subscriptionRepository.findByBarId(barId)
         .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for bar: " + barId));
     if (req.status() != null) sub.setStatus(SubscriptionStatus.valueOf(req.status()));
     if (req.trialType() != null) sub.setTrialType(TrialType.valueOf(req.trialType()));
     if (req.startDate() != null) sub.setStartDate(req.startDate());
     if (req.endDate() != null) sub.setEndDate(req.endDate());
     subscriptionRepository.save(sub);
 }
 
 public BarSummaryResponse getBarById(Long barId) {
	    Bar bar = barRepository.findById(barId)
	        .orElseThrow(() -> new ResourceNotFoundException("Bar not found: " + barId));
	    Subscription sub = subscriptionRepository.findByBarId(barId).orElse(null);
	    return new BarSummaryResponse(
	        bar.getBarId(), bar.getBarName(), bar.getOwnerName(), bar.getPhone(),
	        bar.getEmail(), bar.getLicenseNumber(), bar.getCity(), bar.getState(),
	        bar.getStatus().name(),
	        sub != null ? sub.getStatus().name() : "NONE",
	        sub != null ? sub.getEndDate() : null
	    );
	}
 
 
 
 
}