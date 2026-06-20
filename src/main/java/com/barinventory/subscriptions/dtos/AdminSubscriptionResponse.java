package com.barinventory.subscriptions.dtos;

 

import java.time.LocalDateTime;

public record AdminSubscriptionResponse(
        Long subscriptionId,
        Long barId,
        String barName,
        String status,
        String trialType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long daysRemaining
) {}