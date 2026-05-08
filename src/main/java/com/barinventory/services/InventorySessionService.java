package com.barinventory.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barinventory.entities.Bar;
import com.barinventory.entities.BrandSize;
import com.barinventory.entities.InventorySession;
import com.barinventory.entities.SessionStatus;
import com.barinventory.entities.StockroomInventory;
import com.barinventory.repos.BarRepository;
import com.barinventory.repos.BrandSizeRepository;
import com.barinventory.repos.InventorySessionRepository;
import com.barinventory.repos.StockroomInventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventorySessionService {

    private final InventorySessionRepository sessionRepo;
 
    private final BrandSizeRepository brandSizeRepo;
    private final BarRepository barRepo;
    private final StockroomInventoryRepository stockroomRepo;

    /*
     -----------------------------------------
     CREATE SESSION
     -----------------------------------------
    */
    public InventorySession createSession(Long barId, String sessionName) {

        Bar bar = barRepo.findById(barId)
                .orElseThrow(() -> new RuntimeException("Bar not found"));

        if (sessionRepo.existsByBarBarIdAndStatus(barId, SessionStatus.OPEN)) {
            throw new RuntimeException("An OPEN session already exists");
        }

        InventorySession session = new InventorySession();

        session.setSessionName(sessionName);
        session.setSessionDate(LocalDateTime.now());
        session.setStatus(SessionStatus.OPEN);
        session.setBar(bar);

        // save into NEW variable
        InventorySession savedSession = sessionRepo.save(session);
        // 🔥 ALWAYS initialize stockroom (NO CONDITIONS)
        List<BrandSize> brandSizes =
                brandSizeRepo.findByBarBarId(barId);

        List<StockroomInventory> stocks = brandSizes.stream().map(bs -> {

            StockroomInventory s = new StockroomInventory();

            s.setSession(session);
            s.setBar(bar);
            s.setBrandSize(bs);

            s.setOpeningStock(0);
            s.setReceivedStock(0);
            s.setClosingStock(0);
            s.setSaleStock(0);

            return s;
        }).toList();

        stockroomRepo.saveAll(stocks);

        return session;
    }
       
    /*
     -----------------------------------------
     CLOSE SESSION
     -----------------------------------------
    */
    public void closeSession(Long sessionId) {

        InventorySession session =
                sessionRepo.findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"
                        ));

        if (session.getStatus()
                == SessionStatus.CLOSED) {

            throw new RuntimeException(
                    "Session already closed"
            );
        }

        session.setStatus(SessionStatus.CLOSED);
    }
}