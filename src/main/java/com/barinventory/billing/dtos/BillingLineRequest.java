package com.barinventory.billing.dtos;
 
 

public record BillingLineRequest(
        Long depotBrandId,
        Long depotBrandSizeId,
        String brandName,
        Integer sizeMl,
        Double purchasePrice,
        Integer quantity
) {}