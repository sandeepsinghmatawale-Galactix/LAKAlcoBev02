package com.barinventory.billing.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "billing_lines")
@Getter
@Setter
public class BillingLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long billingLineId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "billing_id", nullable = false)
	private BillingHeader billingHeader;

	@Column(nullable = false)
	private Long depotBrandId;

	@Column(nullable = false)
	private Long depotBrandSizeId;

	@Column(nullable = false)
	private String brandName;

	@Column(nullable = false)
	private Integer sizeMl;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal purchasePrice;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal lineTotal;

	@Column(nullable = false)
	private LocalDateTime createdAt;
}