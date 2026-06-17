package com.barinventory.admin.controllers;

import java.util.Map;

//admin/controllers/AdminDashboardController.java
 

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barinventory.admin.dtos.BarSummaryResponse;
import com.barinventory.admin.dtos.EditBarRequest;
import com.barinventory.admin.dtos.EditSubscriptionRequest;
import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.admin.services.AdminBarService;
import com.barinventory.admin.services.AdminOnboardingService;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
 

 
@RequiredArgsConstructor
public class AdminDashboardController {
 private final AdminOnboardingService onboardingService;
 private final AdminBarService barService;
 private final BarRepository barRepository;
 private final SubscriptionRepository subscriptionRepository;

 //@GetMapping("/admin/dashboard")
 public String dashboard(Model model) {
     model.addAttribute("onboardRequest", new OnboardStoreRequest());
     model.addAttribute("bars", barService.getAllBars());
     model.addAttribute("stats", Map.of(
         "totalBars", barRepository.count(),
         "totalBrands", 0,
         "activeSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.SUBSCRIBED)
     ));
     return "admin-dashboard";
 }

// @PostMapping("/admin/onboard")
 public String onboard(@ModelAttribute OnboardStoreRequest request, RedirectAttributes ra) {
     onboardingService.onboardNewStore(request);
     ra.addAttribute("success", true);
     return "redirect:/admin/dashboard";
 }

 //@PatchMapping("/admin/bars/{barId}")
 @ResponseBody
 public BarSummaryResponse editBar(@PathVariable Long barId, @RequestBody EditBarRequest req) {
     return barService.editBar(barId, req);
 }

 //@PatchMapping("/admin/bars/{barId}/subscription")
 @ResponseBody
 public void editSubscription(@PathVariable Long barId, @RequestBody EditSubscriptionRequest req) {
     barService.editSubscription(barId, req);
 }
}