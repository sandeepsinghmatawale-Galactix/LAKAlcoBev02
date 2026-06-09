package com.barinventory.subscriptions.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByBarId(Long barId);
    
    long countByStatus(SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate >= :windowStart AND s.endDate <= :windowEnd AND s.status = 'SUBSCRIBED'")
    List<Subscription> findSubscriptionsExpiringBetween(
            @Param("windowStart") LocalDateTime windowStart, 
            @Param("windowEnd") LocalDateTime windowEnd
    );
}