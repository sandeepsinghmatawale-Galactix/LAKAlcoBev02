package com.barinventory.inventory.entities;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY) // Safe: inside the same package
    @JoinColumn(name = "session_id")
    private InventorySession session;
    
    @Column(name = "bar_id", nullable = false)
    private Long barId; // ✅ Decoupled ID

    @Column(name = "depot_brand_size_id", nullable = false)
    private Long brandSizeId; // ✅ Decoupled ID pointing to Admin Depot Catalog

    private Integer openingStock;
    private Integer receivedStock;
    private Integer closingStock;
    private Integer saleStock;
}