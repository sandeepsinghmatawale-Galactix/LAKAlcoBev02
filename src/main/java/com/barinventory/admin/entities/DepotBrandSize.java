package com.barinventory.admin.entities;

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
@Table(name = "depot_brand_size")
@Getter @Setter
public class DepotBrandSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandSizeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private DepotBrand brand;

    @Column(nullable = false)
    private Integer sizeMl;
}