package com.barinventory.reports.dtos;



import java.math.BigDecimal;

public record InventoryReportLineResponse(
        String section,
        String wellName,
        String brandName,
        Integer sizeMl,
        Integer openingStock,
        Integer receivedStock,
        Integer closingStock,
        Integer saleStock,
        BigDecimal sellingPrice,
        BigDecimal saleAmount,
        BigDecimal closingStockValue
) {}