package com.barinventory.billing.dtos;

 

import java.util.List;

public record BillingSaveRequest(
        String billingName,
        Double budgetAmount,
        List<BillingLineRequest> lines
) {}