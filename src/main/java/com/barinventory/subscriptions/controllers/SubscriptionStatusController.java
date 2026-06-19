package com.barinventory.subscriptions.controllers;

 

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.config.SecurityUtils;
import com.barinventory.inventory.repos.WellRepository;
import com.barinventory.subscriptions.dtos.SubscriptionStatusResponse;
import com.barinventory.subscriptions.services.SubscriptionViewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionStatusController {

    private final SubscriptionViewService subscriptionViewService;
    private final WellRepository wellRepository;

    @GetMapping("/my-status")
    public SubscriptionStatusResponse myStatus() {
        return subscriptionViewService.getMySubscriptionStatus();
    }
    
    @GetMapping("/my/count")
    public long myWellsCount() {
        Long barId = SecurityUtils.getBarId();
        return wellRepository.countByBarIdAndActiveTrue(barId);
    }
    
    
}