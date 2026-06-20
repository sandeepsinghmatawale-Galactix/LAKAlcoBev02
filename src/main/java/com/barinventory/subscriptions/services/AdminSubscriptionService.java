package com.barinventory.subscriptions.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.subscriptions.dtos.AdminSubscriptionResponse;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	private final BarRepository barRepository;

	public List<AdminSubscriptionResponse> getAllSubscriptions() {
		return subscriptionRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional
	public AdminSubscriptionResponse extendSubscription(Long subscriptionId, Integer daysToAdd) {

		if (daysToAdd == null || daysToAdd <= 0) {
			throw new IllegalArgumentException("Days to add must be greater than zero.");
		}

		Subscription subscription = subscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

		LocalDateTime now = LocalDateTime.now();

		LocalDateTime baseDate = subscription.getEndDate();

		if (baseDate == null || baseDate.isBefore(now)) {
			baseDate = now;
		}

		subscription.setEndDate(baseDate.plusDays(daysToAdd));
		subscription.setStatus(SubscriptionStatus.SUBSCRIBED);

		Subscription saved = subscriptionRepository.save(subscription);

		return toResponse(saved);
	}

	private AdminSubscriptionResponse toResponse(Subscription sub) {

		Bar bar = barRepository.findById(sub.getBarId()).orElse(null);

		long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), sub.getEndDate().toLocalDate());

		return new AdminSubscriptionResponse(sub.getId(), sub.getBarId(),
				bar != null ? bar.getBarName() : "Unknown Bar", sub.getStatus().name(), sub.getTrialType().name(),
				sub.getStartDate(), sub.getEndDate(), daysRemaining);
	}
}