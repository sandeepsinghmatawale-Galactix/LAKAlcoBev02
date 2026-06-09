package com.barinventory.inventory.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.inventory.entities.BarProductPricing;
import com.barinventory.inventory.entities.InventorySession;
import com.barinventory.inventory.entities.SessionStatus;
import com.barinventory.inventory.entities.StockroomInventory;
import com.barinventory.inventory.repos.BarProductPricingRepository;
import com.barinventory.inventory.repos.InventorySessionRepository;
import com.barinventory.inventory.repos.StockroomInventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventorySessionService {

    private final InventorySessionRepository sessionRepo;
    private final StockroomInventoryRepository stockroomRepo;
    private final BarProductPricingRepository productPricingRepo;
    private final BarRepository barRepository;

    /*
     -----------------------------------------
     CREATE SESSION
     -----------------------------------------
    */
    public InventorySession createSession(Long barId, String sessionName) {

        if (sessionRepo.existsByBarBarIdAndStatus(barId, SessionStatus.OPEN)) {
            throw new RuntimeException("An OPEN session already exists for this establishment.");
        }

        Bar bar = barRepository.findById(barId)
                .orElseThrow(() -> new RuntimeException("Bar not found with id: " + barId));

        InventorySession session = new InventorySession();
        session.setSessionName(sessionName);
        session.setSessionDate(LocalDateTime.now());
        session.setStatus(SessionStatus.OPEN);
        session.setBar(bar);
        InventorySession savedSession = sessionRepo.save(session);

        List<BarProductPricing> assignedProducts = productPricingRepo.findByBarId(barId);

        List<StockroomInventory> stocks = assignedProducts.stream().map(product -> {
            StockroomInventory s = new StockroomInventory();
            s.setSession(savedSession);
            s.setBarId(barId);
            s.setBrandSizeId(product.getBrandSizeId());
            s.setOpeningStock(0);
            s.setReceivedStock(0);
            s.setClosingStock(0);
            s.setSaleStock(0);
            return s;
        }).toList();

        stockroomRepo.saveAll(stocks);
        return savedSession;
    }

    /*
     -----------------------------------------
     CLOSE SESSION
     -----------------------------------------
    */
    public void closeSession(Long sessionId) {
        InventorySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new RuntimeException("Session already closed");
        }

        session.setStatus(SessionStatus.CLOSED);
    }

    public List<InventorySession> getAllSessionsForBar(Long barId) {
        return sessionRepo.findByBarBarId(barId); // ✅ returns ALL sessions, not just OPEN
    }

    public Optional<InventorySession> getActiveSession(Long barId) {
        return sessionRepo.findByBarBarIdAndStatus(barId, SessionStatus.OPEN); // ✅ fixed
    }

    public Optional<InventorySession> getLatestSessionByBar(Long barId, SessionStatus status) {
        return sessionRepo.findTopByBarBarIdAndStatusOrderBySessionIdDesc(barId, status);
    }

    public boolean hasActiveSession(Long barId) {
        return sessionRepo.existsByBarBarIdAndStatus(barId, SessionStatus.OPEN);
    }
}