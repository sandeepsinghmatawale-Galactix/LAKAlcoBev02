package com.barinventory.inventory.entities;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "well_inventory")
@Getter
@Setter
public class WellInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wellInventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private InventorySession session;

    @Column(name = "bar_id", nullable = false)
    private Long barId; // ✅ Decoupled ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "well_id")
    private Well well;

    // ✅ FIXED: Maps straight to the custom bar item markup template to calculate the financial amounts correctly
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_product_pricing_id", nullable = false)
    private BarProductPricing productPricing;

    private Integer openingStock;
    private Integer receivedStock;
    private Integer closingStock;
    private Integer saleStock;

    private BigDecimal amount; // Calculated: saleStock * productPricing.getSellingPrice()

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;
}