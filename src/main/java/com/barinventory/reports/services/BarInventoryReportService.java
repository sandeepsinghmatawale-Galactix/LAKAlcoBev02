package com.barinventory.reports.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.barinventory.config.SecurityUtils;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.SessionStatus;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.entities.WellInventory;
import com.barinventory.inventory.repos.BarProductPricingRepository;
import com.barinventory.inventory.repos.InventorySessionRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;
import com.barinventory.inventory.repos.WellInventoryRepository;
import com.barinventory.reports.dtos.InventoryReportLineResponse;
import com.barinventory.reports.dtos.InventoryReportResponse;
import com.barinventory.reports.dtos.WellReportResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BarInventoryReportService {

	private final InventorySessionRepository sessionRepository;
	private final StockroomInventoryRepository stockroomRepository;
	private final WellInventoryRepository wellInventoryRepository;
	private final BarProductPricingRepository pricingRepository;

	public InventoryReportResponse getDailyReport(LocalDate reportDate) {

		Long barId = SecurityUtils.getBarId();

		LocalDateTime from = reportDate.atStartOfDay();
		LocalDateTime to = reportDate.atTime(LocalTime.MAX);

		List<InventorySession> sessions = sessionRepository
				.findByBarBarIdAndStatusAndSessionDateBetweenOrderBySessionDateDesc(barId, SessionStatus.CLOSED, from,
						to);

		return buildReport(barId, reportDate, reportDate, sessions);
	}

	public InventoryReportResponse getCustomReport(LocalDate fromDate, LocalDate toDate) {

		Long barId = SecurityUtils.getBarId();

		LocalDateTime from = fromDate.atStartOfDay();
		LocalDateTime to = toDate.atTime(LocalTime.MAX);

		List<InventorySession> sessions = sessionRepository
				.findByBarBarIdAndStatusAndSessionDateBetweenOrderBySessionDateDesc(barId, SessionStatus.CLOSED, from,
						to);

		return buildReport(barId, fromDate, toDate, sessions);
	}

	private InventoryReportResponse buildReport(Long barId, LocalDate fromDate, LocalDate toDate,
			List<InventorySession> sessions) {

		Map<Long, BarProductPricing> pricingByBrandSize = buildPricingMap(barId);

		List<InventoryReportLineResponse> stockroomLines = new ArrayList<>();

		Map<Long, WellAccumulator> wellMap = new LinkedHashMap<>();

		BigDecimal stockroomSaleTotal = BigDecimal.ZERO;
		BigDecimal wellsSaleTotal = BigDecimal.ZERO;

		BigDecimal stockroomClosingValue = BigDecimal.ZERO;
		BigDecimal wellsClosingValue = BigDecimal.ZERO;

		for (InventorySession session : sessions) {

			Long sessionId = session.getSessionId();

			List<StockroomInventory> stockroomRows = stockroomRepository.findByBarIdAndSessionSessionId(barId,
					sessionId);

			for (StockroomInventory row : stockroomRows) {

				BarProductPricing pricing = pricingByBrandSize.get(row.getDepotBrandSizeId());

				BigDecimal sellingPrice = getSellingPrice(pricing);

				int opening = safeInt(row.getOpeningStock());
				int received = safeInt(row.getReceivedStock());
				int closing = safeInt(row.getClosingStock());
				int sale = resolveSaleStock(row.getSaleStock(), opening, received, closing);

				BigDecimal saleAmount = sellingPrice.multiply(BigDecimal.valueOf(sale)).setScale(2,
						RoundingMode.HALF_UP);

				BigDecimal closingValue = sellingPrice.multiply(BigDecimal.valueOf(closing)).setScale(2,
						RoundingMode.HALF_UP);

				stockroomSaleTotal = stockroomSaleTotal.add(saleAmount);

				stockroomClosingValue = stockroomClosingValue.add(closingValue);

				stockroomLines.add(new InventoryReportLineResponse("STOCKROOM", null,
						getBrandName(pricing, row.getDepotBrandSizeId()), getSizeMl(pricing), opening, received,
						closing, sale, sellingPrice, saleAmount, closingValue));
			}

			List<WellInventory> wellRows = wellInventoryRepository.findByBarIdAndSessionSessionId(barId, sessionId);

			for (WellInventory row : wellRows) {

				BarProductPricing pricing = row.getProductPricing();

				BigDecimal sellingPrice = getSellingPrice(pricing);

				int opening = safeInt(row.getOpeningStock());
				int received = safeInt(row.getReceivedStock());
				int closing = safeInt(row.getClosingStock());
				int sale = resolveSaleStock(row.getSaleStock(), opening, received, closing);

				BigDecimal saleAmount = sellingPrice.multiply(BigDecimal.valueOf(sale)).setScale(2,
						RoundingMode.HALF_UP);

				BigDecimal closingValue = sellingPrice.multiply(BigDecimal.valueOf(closing)).setScale(2,
						RoundingMode.HALF_UP);

				wellsSaleTotal = wellsSaleTotal.add(saleAmount);

				wellsClosingValue = wellsClosingValue.add(closingValue);

				Long wellId = row.getWell() != null ? row.getWell().getWellId() : 0L;

				String wellName = row.getWell() != null ? row.getWell().getWellName() : "Unknown Well";

				WellAccumulator acc = wellMap.computeIfAbsent(wellId, id -> new WellAccumulator(wellId, wellName));

				InventoryReportLineResponse line = new InventoryReportLineResponse("WELL", wellName,
						getBrandName(pricing, null), getSizeMl(pricing), opening, received, closing, sale, sellingPrice,
						saleAmount, closingValue);

				acc.lines.add(line);
				acc.wellSaleTotal = acc.wellSaleTotal.add(saleAmount);
				acc.wellClosingStockValue = acc.wellClosingStockValue.add(closingValue);
			}
		}

		List<WellReportResponse> wellReports = wellMap.values().stream().map(acc -> new WellReportResponse(acc.wellId,
				acc.wellName, acc.wellSaleTotal, acc.wellClosingStockValue, acc.lines)).toList();

		BigDecimal grandSaleTotal = stockroomSaleTotal.add(wellsSaleTotal);

		BigDecimal totalClosingStockValue = stockroomClosingValue.add(wellsClosingValue);

		return new InventoryReportResponse(barId, fromDate, toDate, sessions.size(), stockroomSaleTotal, wellsSaleTotal,
				grandSaleTotal, stockroomClosingValue, wellsClosingValue, totalClosingStockValue, stockroomLines,
				wellReports);
	}

	private Map<Long, BarProductPricing> buildPricingMap(Long barId) {

		Map<Long, BarProductPricing> map = new LinkedHashMap<>();

		for (BarProductPricing pricing : pricingRepository.findByBarId(barId)) {
			if (pricing.getDepotBrandSizeId() != null) {
				map.put(pricing.getDepotBrandSizeId(), pricing);
			}
		}

		return map;
	}

	private BigDecimal getSellingPrice(BarProductPricing pricing) {

		if (pricing == null || pricing.getSellingPrice() == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(pricing.getSellingPrice()).setScale(2, RoundingMode.HALF_UP);
	}

	private String getBrandName(BarProductPricing pricing, Long fallbackBrandSizeId) {

		if (pricing != null && pricing.getCachedBrandName() != null) {
			return pricing.getCachedBrandName();
		}

		if (fallbackBrandSizeId != null) {
			return "Brand Size ID " + fallbackBrandSizeId;
		}

		return "Unknown Product";
	}

	private Integer getSizeMl(BarProductPricing pricing) {

		if (pricing == null || pricing.getCachedSizeMl() == null) {
			return 0;
		}

		return pricing.getCachedSizeMl();
	}

	private int safeInt(Integer value) {
		return value != null ? value : 0;
	}

	private int resolveSaleStock(Integer saleStock, int opening, int received, int closing) {

		if (saleStock != null) {
			return saleStock;
		}

		return Math.max(0, opening + received - closing);
	}

	private static class WellAccumulator {

		private final Long wellId;
		private final String wellName;

		private BigDecimal wellSaleTotal = BigDecimal.ZERO;

		private BigDecimal wellClosingStockValue = BigDecimal.ZERO;

		private final List<InventoryReportLineResponse> lines = new ArrayList<>();

		private WellAccumulator(Long wellId, String wellName) {
			this.wellId = wellId;
			this.wellName = wellName;
		}
	}
}