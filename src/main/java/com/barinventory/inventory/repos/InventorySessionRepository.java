package com.barinventory.inventory.repos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.SessionStatus;

@Repository
public interface InventorySessionRepository extends JpaRepository<InventorySession, Long> {

    Optional<InventorySession> findTopByStatusOrderBySessionIdDesc(SessionStatus status);

    Optional<InventorySession> findTopByBarBarIdAndStatusOrderBySessionIdDesc(Long barId, SessionStatus status);

    Optional<InventorySession> findByBarBarIdAndStatus(Long barId, SessionStatus status);

    boolean existsByBarBarIdAndStatus(Long barId, SessionStatus status);

    Optional<InventorySession> findBySessionIdAndBarBarId(Long sessionId, Long barId);

    List<InventorySession> findByBarBarId(Long barId);
    
      

    List<InventorySession> findByBarBarIdAndSessionDateBetweenOrderBySessionDateDesc(
        Long barId, LocalDateTime from, LocalDateTime to);
    
    
    List<InventorySession> findByBarBarIdAndStatusAndSessionDateBetweenOrderBySessionDateDesc(
            Long barId,
            SessionStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
    
}