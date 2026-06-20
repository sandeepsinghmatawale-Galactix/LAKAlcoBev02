package com.barinventory.billing.dtos;

 

import java.math.BigDecimal;

public record BillingLineResponse(
        Long billingLineId,
        Long depotBrandId,
        Long depotBrandSizeId,
        String brandName,
        Integer sizeMl,
        BigDecimal purchasePrice,
        Integer quantity,
        BigDecimal lineTotal
) {}