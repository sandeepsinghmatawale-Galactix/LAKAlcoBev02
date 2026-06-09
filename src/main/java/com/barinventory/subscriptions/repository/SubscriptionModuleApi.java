package com.barinventory.subscriptions.repository;

import com.barinventory.subscriptions.enums.SubscriptionStatus;

public interface SubscriptionModuleApi {
    SubscriptionStatus getSubscriptionStatusForBar(Long barId);
}