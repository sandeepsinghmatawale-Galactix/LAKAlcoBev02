package com.barinventory.auth.entities;

import java.util.List;

import com.barinventory.inventory.entities.InventorySession;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public class Bar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long barId;

    private String barName;

    // Optional (only if needed)
    @OneToMany(mappedBy = "bar")
    private List<InventorySession> sessions;
}