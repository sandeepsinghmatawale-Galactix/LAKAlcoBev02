package com.barinventory.subscriptions.dtos;

 

import java.time.LocalDateTime;

public record SubscriptionStatusResponse(
        Long barId,
        String status,
        String trialType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long daysRemaining
) {}