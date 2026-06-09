package com.barinventory.admin.services;

import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.DashboardStatsDTO;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.auth.repos.BrandRepository;
import com.barinventory.subscriptions.services.SubscriptionStatsService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final BarRepository barRepository;
    private final BrandRepository brandRepository;
    private final SubscriptionStatsService subscriptionStatsService;

    @Transactional // Optimization for read-only tracking queries
    public DashboardStatsDTO getDashboardStats() {
        
        long totalBars = barRepository.count();
        long totalBrands = brandRepository.count();
        long activeSubs = subscriptionStatsService.getActiveSubscriptionCount();

        return new DashboardStatsDTO(totalBars, totalBrands, activeSubs);
    }
}