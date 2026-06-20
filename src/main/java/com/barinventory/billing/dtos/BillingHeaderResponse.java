package com.barinventory.billing.dtos;

 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BillingHeaderResponse(
        Long billingId,
        Long barId,
        String billingName,
        BigDecimal budgetAmount,
        BigDecimal totalAmount,
        BigDecimal budgetRemaining,
        Integer totalItems,
        Integer totalQuantity,
        String budgetStatus,
        LocalDateTime createdAt,
        List<BillingLineResponse> lines
) {}
