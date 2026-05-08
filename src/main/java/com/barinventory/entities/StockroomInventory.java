package com.barinventory.entities;

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
@Table(name = "stockroom_inventory")
@Getter
@Setter
public class StockroomInventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stockroomId;

	@ManyToOne
	@JoinColumn(name = "session_id")
	private InventorySession session;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bar_id", nullable = false)
	private Bar bar;

	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_size_id", nullable = false)
	private BrandSize brandSize;

	private Integer openingStock;
	private Integer receivedStock;
	private Integer closingStock;
	private Integer saleStock;
}