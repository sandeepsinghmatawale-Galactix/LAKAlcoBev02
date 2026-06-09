package com.barinventory.admin.dtos;

import java.time.LocalDateTime;

import com.barinventory.subscriptions.enums.TrialType;

import lombok.Data;

@Data
public class OnboardStoreRequest {
    private String storeName;
    private String adminUsername;
    private String adminPassword;
    private TrialType trialType;
    private LocalDateTime customStartDate;
    private LocalDateTime customEndDate;
}