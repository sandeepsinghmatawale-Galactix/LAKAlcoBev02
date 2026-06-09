package com.barinventory.subscriptions.entities;



import java.time.LocalDateTime;

import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.enums.TrialType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long barId; // Links directly to the store/bar id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // SUBSCRIBED, UNSUBSCRIBED, PENDING, BLOCKED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrialType trialType; // WEEKLY, MONTHLY, THREE_MONTH, YEARLY, CUSTOM

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;
}