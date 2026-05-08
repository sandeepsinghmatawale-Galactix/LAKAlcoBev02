package com.barinventory.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="well_distribution")
@Getter
@Setter
public class WellDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wellDistributionId;

    @ManyToOne
    @JoinColumn(name="distribution_id")
    private Distribution distribution;

    @ManyToOne
    @JoinColumn(name="well_id")
    private Well well;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_size_id", nullable = false)
    private BrandSize brandSize;

    private Integer distributedQty;

    private LocalDateTime distributedAt;
    
    @Version
    private Long version;
}