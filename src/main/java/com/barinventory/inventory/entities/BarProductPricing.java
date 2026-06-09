package com.barinventory.inventory.entities;
 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "bar_product_pricings", uniqueConstraints = @UniqueConstraint(columnNames = {"bar_id", "depot_brand_size_id"}))
@Data
public class BarProductPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bar_id", nullable = false)
    private Long barId;

    @Column(name = "depot_brand_id", nullable = false)
    private Long brandId;

    @Column(name = "depot_brand_size_id", nullable = false)
    private Long brandSizeId;

    // Denormalized/cached fields for instant page rendering performance
    private String cachedBrandName;
    private Integer cachedSizeMl;
    private Double cachedMrp;

    private Double purchasePrice; 
    private Double sellingPrice; 
}