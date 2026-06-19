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
    private final AuthModuleApi authModuleApi;
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 */1 * * * ?")
    public void checkAndNotifyExpiringSubscriptions() {

        log.info("Subscription expiration monitoring cron job invoked.");

        notifyExpiringSubscriptions(15);
        notifyExpiringSubscriptions(7);
        notifyExpiringSubscriptions(3);
        notifyExpiringSubscriptions(1);
    }

    private void notifyExpiringSubscriptions(int daysBeforeExpiry) {

        LocalDate targetDate = LocalDate.now().plusDays(daysBeforeExpiry);

        LocalDateTime windowStart = targetDate.atStartOfDay();
        LocalDateTime windowEnd = targetDate.atTime(LocalTime.MAX);

        List<Subscription> expiringSubscriptions =
                subscriptionRepository.findSubscriptionsExpiringBetween(
                        windowStart,
                        windowEnd
                );

        log.info(
                "Found {} subscription(s) expiring in {} day(s).",
                expiringSubscriptions.size(),
                daysBeforeExpiry
        );

        for (Subscription sub : expiringSubscriptions) {

            try {
                String ownerEmail =
                        authModuleApi.findOwnerEmailByBarId(sub.getBarId());

                if (ownerEmail == null || ownerEmail.isBlank()) {
                    log.warn(
                            "No owner email found for Bar ID: {}. Skipping reminder.",
                            sub.getBarId()
                    );
                    continue;
                }

                sendExpiryAlertEmail(
                        ownerEmail,
                        sub.getEndDate(),
                        daysBeforeExpiry
                );

                log.info(
                        "Subscription expiry reminder sent to {} for Bar ID: {}, days remaining: {}",
                        ownerEmail,
                        sub.getBarId(),
                        daysBeforeExpiry
                );

            } catch (Exception e) {
                log.error(
                        "Failed to send subscription expiry reminder for Bar ID: {}",
                        sub.getBarId(),
                        e
                );
            }
        }
    }

    private void sendExpiryAlertEmail(
            String recipientEmail,
            LocalDateTime expiryDate,
            int daysRemaining
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipientEmail);

        message.setSubject(
                "⚠️ Subscription Expiry Reminder - "
                        + daysRemaining
                        + " Day(s) Remaining"
        );

        message.setText(
                "Dear Partner,\n\n"
                        + "This is a reminder that your Bar Inventory subscription will expire soon.\n\n"
                        + "Expiry Date: " + expiryDate.toLocalDate() + "\n"
                        + "Days Remaining: " + daysRemaining + "\n\n"
                        + "Please renew your subscription before the expiry date to avoid service interruption.\n\n"
                        + "Regards,\n"
                        + "Bar Inventory Team"
        );

        mailSender.send(message);
    }
}