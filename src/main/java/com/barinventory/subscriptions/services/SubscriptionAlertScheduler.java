package com.barinventory.subscriptions.services;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.barinventory.auth.servcies.AuthModuleApi;
import com.barinventory.subscriptions.entities.Subscription;
import com.barinventory.subscriptions.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionAlertScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final AuthModuleApi authModuleApi; // ✅ Clean cross-module API link
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8:00 AM
    public void checkAndNotifyExpiringSubscriptions() {
        log.info("Subscription expiration monitoring cron job invoked.");

        LocalDate targetDate = LocalDate.now().plusDays(5);
        LocalDateTime windowStart = targetDate.atStartOfDay();
        LocalDateTime windowEnd = targetDate.atTime(LocalTime.MAX);

        // ✅ Pure Spring Data JPA Repository call
        List<Subscription> expiringSubscriptions = subscriptionRepository.findSubscriptionsExpiringBetween(windowStart, windowEnd);

        for (Subscription sub : expiringSubscriptions) {
            try {
                // ✅ Fetch owner email through the official API rather than an internal DB crawl
                String ownerEmail = authModuleApi.findOwnerEmailByBarId(sub.getBarId());

                sendExpiryAlertEmail(ownerEmail, sub.getEndDate());
                
            } catch (Exception e) {
                log.error("Failed to transmit expiration notification alert for Bar ID: {}", sub.getBarId(), e);
            }
        }
    }

    private void sendExpiryAlertEmail(String recipientEmail, LocalDateTime expiryDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("⚠️ Urgent: Your Bar Inventory Subscription Expires in 5 Days!");
        message.setText("Dear Partner,\n\n" +
                "Your premium access window is scheduled to expire on " + expiryDate.toLocalDate() + ".\n\n" +
                "Best regards,\nPlatform Management Team");
        
        mailSender.send(message);
    }
}