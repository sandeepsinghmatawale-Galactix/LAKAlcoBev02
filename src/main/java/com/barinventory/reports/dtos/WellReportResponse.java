package com.barinventory.reports.dtos;
 

import java.math.BigDecimal;
import java.util.List;

public record WellReportResponse(
        Long wellId,
        String wellName,
        BigDecimal wellSaleTotal,
        BigDecimal wellClosingStockValue,
        List<InventoryReportLineResponse> lines
) {}