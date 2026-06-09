package com.barinventory.auth.servcies;

import org.springframework.stereotype.Service;

import com.barinventory.auth.repos.BarUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthModuleApi {

    private final BarUserRepository barUserRepository;

    // Custom query method derived cleanly by Spring Data JPA
    public String findOwnerEmailByBarId(Long barId) {
        return barUserRepository.findUsernameByBarIdAndRole(barId, "BUSINESS_OWNER")
                .orElseThrow(() -> new RuntimeException("Business owner email not found for bar ID: " + barId));
    }
}