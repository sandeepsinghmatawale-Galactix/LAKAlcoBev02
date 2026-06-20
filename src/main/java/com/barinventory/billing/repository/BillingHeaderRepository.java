package com.barinventory.billing.repository;
 

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.billing.entities.BillingHeader;

public interface BillingHeaderRepository extends JpaRepository<BillingHeader, Long> {

    List<BillingHeader> findByBarIdOrderByCreatedAtDesc(Long barId);
}