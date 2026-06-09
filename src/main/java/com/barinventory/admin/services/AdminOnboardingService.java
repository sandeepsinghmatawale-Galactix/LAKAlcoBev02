package com.barinventory.admin.services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.entities.BarUser;
import com.barinventory.auth.enums.Role;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.enums.TrialType;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOnboardingService {

    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void onboardNewStore(OnboardStoreRequest request) {
        // 1. Create the Store (Bar Entity)
        Bar bar = new Bar();
        bar.setBarName(request.getStoreName());
        entityManager.persist(bar);

        // 2. Create the Business Owner Profile
        BarUser user = new BarUser();
        user.setUsername(request.getAdminUsername());
        user.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        user.setBarId(bar.getBarId());
        user.setRole(Role.BUSINESS_OWNER);
        entityManager.persist(user);

        // 3. Establish System Time Constraints for Subscription
        Subscription sub = new Subscription();
        sub.setBarId(bar.getBarId());
        sub.setStatus(SubscriptionStatus.SUBSCRIBED);
        sub.setTrialType(request.getTrialType());

        if (request.getTrialType() == TrialType.CUSTOM) {
            sub.setStartDate(request.getCustomStartDate());
            sub.setEndDate(request.getCustomEndDate());
        } else {
            sub.setStartDate(LocalDateTime.now());
            sub.setEndDate(calculateExpiry(request.getTrialType()));
        }
        entityManager.persist(sub);
    }

    private LocalDateTime calculateExpiry(TrialType type) {
        return switch (type) {
            case WEEKLY -> LocalDateTime.now().plusWeeks(1);
            case MONTHLY -> LocalDateTime.now().plusMonths(1);
            case THREE_MONTH -> LocalDateTime.now().plusMonths(3);
            case YEARLY -> LocalDateTime.now().plusYears(1);
            default -> LocalDateTime.now().plusMonths(1);
        };
    }
}