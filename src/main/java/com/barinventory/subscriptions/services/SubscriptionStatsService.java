package com.barinventory.subscriptions.services;

import org.springframework.stereotype.Service;

import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionStatsService {

    private final SubscriptionRepository subscriptionRepository;

    public long getActiveSubscriptionCount() {
        return subscriptionRepository.countByStatus(SubscriptionStatus.SUBSCRIBED);
    }
}