package com.barinventory.admin.dtos;

 

public record OpeningStockCatalogSkuResponse(
        Long packId,
        Long brandSizeId,
        Long brandId,
        String brandName,
        Integer sizeMl,
        String packagingType,
        Double mrp
) {}