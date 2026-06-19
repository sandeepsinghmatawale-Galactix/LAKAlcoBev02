package com.barinventory.auth.entities;
 

import java.time.LocalDateTime;

import com.barinventory.admin.enums.BarRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_bar_access")
@Getter
@Setter
public class UserBarAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="bar_id", nullable=false)
    private Long barId;

    @Enumerated(EnumType.STRING)
    @Column(name="bar_role", nullable=false)
    private BarRole barRole;

    @Column(nullable=false)
    private Boolean active = true;

    @Column(name="granted_by")
    private Long grantedBy;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}