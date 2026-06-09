package com.barinventory.inventory.entities;

import java.time.LocalDateTime;

import com.barinventory.auth.entities.Bar;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "inventory_session")
@Getter
@Setter
public class InventorySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    private String sessionName;

    private LocalDateTime sessionDate;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;
}