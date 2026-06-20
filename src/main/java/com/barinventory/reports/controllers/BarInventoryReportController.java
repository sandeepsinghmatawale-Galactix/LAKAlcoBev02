package com.barinventory.reports.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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

    @GetMapping("/weekly")
    @ResponseBody
    public InventoryReportResponse weeklyReport(
            @RequestParam(required = false) LocalDate date) {

        LocalDate baseDate = date != null ? date : LocalDate.now();

        LocalDate fromDate = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate toDate = baseDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return reportService.getCustomReport(fromDate, toDate);
    }

    @GetMapping("/monthly")
    @ResponseBody
    public InventoryReportResponse monthlyReport(
            @RequestParam(required = false) LocalDate date) {

        LocalDate baseDate = date != null ? date : LocalDate.now();

        LocalDate fromDate = baseDate.withDayOfMonth(1);
        LocalDate toDate = baseDate.withDayOfMonth(baseDate.lengthOfMonth());

        return reportService.getCustomReport(fromDate, toDate);
    }

    @GetMapping("/quarterly")
    @ResponseBody
    public InventoryReportResponse quarterlyReport(
            @RequestParam(required = false) LocalDate date) {

        LocalDate baseDate = date != null ? date : LocalDate.now();

        int currentMonth = baseDate.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;

        LocalDate fromDate = LocalDate.of(baseDate.getYear(), quarterStartMonth, 1);
        LocalDate toDate = fromDate.plusMonths(2)
                .withDayOfMonth(fromDate.plusMonths(2).lengthOfMonth());

        return reportService.getCustomReport(fromDate, toDate);
    }

    @GetMapping("/yearly")
    @ResponseBody
    public InventoryReportResponse yearlyReport(
            @RequestParam(required = false) LocalDate date) {

        LocalDate baseDate = date != null ? date : LocalDate.now();

        LocalDate fromDate = LocalDate.of(baseDate.getYear(), 1, 1);
        LocalDate toDate = LocalDate.of(baseDate.getYear(), 12, 31);

        return reportService.getCustomReport(fromDate, toDate);
    }

    @GetMapping("/custom")
    @ResponseBody
    public InventoryReportResponse customReport(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {

        return reportService.getCustomReport(fromDate, toDate);
    }
}