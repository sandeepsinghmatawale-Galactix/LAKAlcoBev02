package com.barinventory.inventory.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.barinventory.inventory.entities.Well;
import com.barinventory.inventory.repos.WellRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WellService {

    private final WellRepository wellRepository;

    public List<Well> getAllWells() {
        return wellRepository.findAll();
    }

    public Well getWellById(Long wellId) {
        return wellRepository.findById(wellId)
                .orElseThrow(() -> new RuntimeException("Well context assignment missing from system layouts."));
    }
    
    public List<Well> getWellsByBar(Long barId) {
        // ✅ Option A: Call the clean flat property finder method name
        return wellRepository.findByBarId(barId); 
    }    
}