package com.barinventory.reports.dtos;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InventoryReportResponse(
        Long barId,
        LocalDate fromDate,
        LocalDate toDate,

        Integer totalSessions,

        BigDecimal stockroomSaleTotal,
        BigDecimal wellsSaleTotal,
        BigDecimal grandSaleTotal,

        BigDecimal stockroomClosingStockValue,
        BigDecimal wellsClosingStockValue,
        BigDecimal totalClosingStockValue,

        List<InventoryReportLineResponse> stockroomLines,
        List<WellReportResponse> wellReports
) {}