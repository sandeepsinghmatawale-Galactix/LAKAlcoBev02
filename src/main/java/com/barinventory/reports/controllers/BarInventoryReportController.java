package com.barinventory.reports.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.barinventory.reports.dtos.InventoryReportResponse;
import com.barinventory.reports.services.BarInventoryReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reports/inventory")
@RequiredArgsConstructor
public class BarInventoryReportController {

    private final BarInventoryReportService reportService;

    @GetMapping
    public String reportPage() {
        return "reports/inventory-report";
    }

    @GetMapping("/daily")
    @ResponseBody
    public InventoryReportResponse dailyReport(
            @RequestParam(required = false) LocalDate date) {

        LocalDate reportDate = date != null ? date : LocalDate.now();

        return reportService.getDailyReport(reportDate);
    }

    @GetMapping("/custom")
    @ResponseBody
    public InventoryReportResponse customReport(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {

        return reportService.getCustomReport(fromDate, toDate);
    }
}