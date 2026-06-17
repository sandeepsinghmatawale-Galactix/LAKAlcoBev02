package com.barinventory.admin.dtos;

//admin/dtos/OpeningStockEntry.java
 

public record OpeningStockEntry(
        Long depotPackId,
        Long depotBrandSizeId,
        Integer openingQty,
        Double purchasePricePerUnit,
        Double sellingPrice,
        String brandName,
        Integer sizeMl,
        String packagingType,
        Double mrp,
        String invoiceRefNo
) {}