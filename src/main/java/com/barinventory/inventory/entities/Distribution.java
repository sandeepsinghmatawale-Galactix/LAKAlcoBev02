package com.barinventory.inventory.entities;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="distribution_table")
@Getter
@Setter
public class Distribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long distributionId;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Optimized to prevent eager table loading issues
    @JoinColumn(name="session_id")
    private InventorySession session;

    private LocalDateTime distributedAt;
    
    @Version
    private Long version;
}