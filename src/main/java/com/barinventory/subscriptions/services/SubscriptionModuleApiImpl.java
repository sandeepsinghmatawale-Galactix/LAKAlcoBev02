package com.barinventory.subscriptions.services;

import org.springframework.stereotype.Service;

import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.repository.SubscriptionModuleApi;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionModuleApiImpl implements SubscriptionModuleApi {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public SubscriptionStatus getSubscriptionStatusForBar(Long barId) {
        return subscriptionRepository.findByBarId(barId)
                .map(subscription -> subscription.getStatus())
                .orElse(SubscriptionStatus.UNSUBSCRIBED);
    }
}