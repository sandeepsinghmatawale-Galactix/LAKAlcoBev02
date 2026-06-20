package com.barinventory.auth.servcies;

import org.springframework.stereotype.Service;

import com.barinventory.auth.enums.Role;
import com.barinventory.auth.repos.BarUserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AuthModuleApi {

    private final BarUserRepository barUserRepository;

    public String findOwnerEmailByBarId(Long barId) {
        return barUserRepository
                .findUsernameByBarIdAndRole(barId, Role.BUSINESS_OWNER)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Business owner email not found for bar ID: " + barId
                        )
                );
    }
}