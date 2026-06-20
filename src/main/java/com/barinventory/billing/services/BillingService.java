package com.barinventory.billing.services;
 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barinventory.billing.dtos.BillingHeaderResponse;
import com.barinventory.billing.dtos.BillingLineRequest;
import com.barinventory.billing.dtos.BillingLineResponse;
import com.barinventory.billing.dtos.BillingSaveRequest;
import com.barinventory.billing.entities.BillingHeader;
import com.barinventory.billing.entities.BillingLine;
import com.barinventory.billing.enums.BillingBudgetStatus;
import com.barinventory.billing.repository.BillingHeaderRepository;
import com.barinventory.config.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingHeaderRepository billingHeaderRepository;

    @Transactional
    public BillingHeaderResponse saveBilling(BillingSaveRequest request) {

        Long barId = SecurityUtils.getBarId();
        Long userId = SecurityUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();

        if (request.lines() == null || request.lines().isEmpty()) {
            throw new IllegalArgumentException("At least one billing item is required.");
        }

        BigDecimal budgetAmount = toMoney(request.budgetAmount());

        BillingHeader header = new BillingHeader();
        header.setBarId(barId);
        header.setBillingName(request.billingName());
        header.setBudgetAmount(budgetAmount);
        header.setCreatedBy(userId);
        header.setCreatedAt(now);
        header.setUpdatedAt(now);

        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BillingLineRequest lineReq : request.lines()) {

            validateLine(lineReq);

            BigDecimal purchasePrice = toMoney(lineReq.purchasePrice());
            int quantity = lineReq.quantity();

            BigDecimal lineTotal = purchasePrice.multiply(BigDecimal.valueOf(quantity));

            BillingLine line = new BillingLine();
            line.setBillingHeader(header);
            line.setDepotBrandId(lineReq.depotBrandId());
            line.setDepotBrandSizeId(lineReq.depotBrandSizeId());
            line.setBrandName(lineReq.brandName());
            line.setSizeMl(lineReq.sizeMl());
            line.setPurchasePrice(purchasePrice);
            line.setQuantity(quantity);
            line.setLineTotal(lineTotal);
            line.setCreatedAt(now);

            header.getLines().add(line);

            totalQuantity += quantity;
            totalAmount = totalAmount.add(lineTotal);
        }

        header.setTotalItems(header.getLines().size());
        header.setTotalQuantity(totalQuantity);
        header.setTotalAmount(totalAmount);
        header.setBudgetRemaining(budgetAmount.subtract(totalAmount));

        if (totalAmount.compareTo(budgetAmount) > 0) {
            header.setBudgetStatus(BillingBudgetStatus.BUDGET_EXCEEDED);
        } else {
            header.setBudgetStatus(BillingBudgetStatus.WITHIN_BUDGET);
        }

        BillingHeader saved = billingHeaderRepository.save(header);

        return toResponse(saved);
    }

    public List<BillingHeaderResponse> getMyBillingHistory() {
        Long barId = SecurityUtils.getBarId();

        return billingHeaderRepository
                .findByBarIdOrderByCreatedAtDesc(barId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BillingHeaderResponse getMyBillingDetails(Long billingId) {
        Long barId = SecurityUtils.getBarId();

        BillingHeader header = billingHeaderRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        if (!header.getBarId().equals(barId)) {
            throw new RuntimeException("You cannot view billing of another bar.");
        }

        return toResponse(header);
    }

    private void validateLine(BillingLineRequest line) {

        if (line.depotBrandId() == null) {
            throw new IllegalArgumentException("Depot brand id is required.");
        }

        if (line.depotBrandSizeId() == null) {
            throw new IllegalArgumentException("Depot brand size id is required.");
        }

        if (line.brandName() == null || line.brandName().isBlank()) {
            throw new IllegalArgumentException("Brand name is required.");
        }

        if (line.sizeMl() == null || line.sizeMl() <= 0) {
            throw new IllegalArgumentException("Valid size is required.");
        }

        if (line.purchasePrice() == null || line.purchasePrice() <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than zero.");
        }

        if (line.quantity() == null || line.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    private BigDecimal toMoney(Double value) {
        if (value == null || value < 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BillingHeaderResponse toResponse(BillingHeader header) {

        List<BillingLineResponse> lineResponses = header.getLines()
                .stream()
                .map(line -> new BillingLineResponse(
                        line.getBillingLineId(),
                        line.getDepotBrandId(),
                        line.getDepotBrandSizeId(),
                        line.getBrandName(),
                        line.getSizeMl(),
                        line.getPurchasePrice(),
                        line.getQuantity(),
                        line.getLineTotal()
                ))
                .toList();

        return new BillingHeaderResponse(
                header.getBillingId(),
                header.getBarId(),
                header.getBillingName(),
                header.getBudgetAmount(),
                header.getTotalAmount(),
                header.getBudgetRemaining(),
                header.getTotalItems(),
                header.getTotalQuantity(),
                header.getBudgetStatus().name(),
                header.getCreatedAt(),
                lineResponses
        );
    }
}