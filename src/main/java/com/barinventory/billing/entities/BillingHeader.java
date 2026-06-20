package com.barinventory.billing.entities;

 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.barinventory.billing.enums.BillingBudgetStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "billing_headers")
@Getter
@Setter
public class BillingHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billingId;

    @Column(nullable = false)
    private Long barId;

    private String billingName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetRemaining = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalItems = 0;

    @Column(nullable = false)
    private Integer totalQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingBudgetStatus budgetStatus = BillingBudgetStatus.WITHIN_BUDGET;

    private Long createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "billingHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillingLine> lines = new ArrayList<>();
}