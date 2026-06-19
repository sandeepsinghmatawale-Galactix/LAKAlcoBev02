package com.barinventory.inventory.dtos;

 

public record UpdateSellingPriceRequest(
        Long pricingId,
        Double sellingPrice
) {}