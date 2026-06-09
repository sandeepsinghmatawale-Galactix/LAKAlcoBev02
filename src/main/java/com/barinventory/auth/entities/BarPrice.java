package com.barinventory.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(
    name = "bar_prices",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bar_id", "brand_id"})
    }
)
@Getter
@Setter
public class BarPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long barPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    // price per unit (peg/bottle/etc)
    @Column(nullable = false)
    private Double price;
}