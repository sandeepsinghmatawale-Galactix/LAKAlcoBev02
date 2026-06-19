package com.barinventory.subscriptions.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.barinventory.config.SecurityUtils;
import com.barinventory.subscriptions.dtos.SubscriptionStatusResponse;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionViewService {

	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionStatusResponse getMySubscriptionStatus() {

		Long barId = SecurityUtils.getBarId();

		Subscription sub = subscriptionRepository.findByBarId(barId)
				.orElseThrow(() -> new RuntimeException("Subscription not found for bar: " + barId));

		long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), sub.getEndDate().toLocalDate());

		return new SubscriptionStatusResponse(sub.getBarId(), sub.getStatus().name(), sub.getTrialType().name(),
				sub.getStartDate(), sub.getEndDate(), daysRemaining);
	}
}