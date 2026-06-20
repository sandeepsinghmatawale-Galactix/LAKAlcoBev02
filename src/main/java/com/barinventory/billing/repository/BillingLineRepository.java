package com.barinventory.billing.repository;

 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.billing.entities.BillingLine;

public interface BillingLineRepository extends JpaRepository<BillingLine, Long> {

    List<BillingLine> findByBillingHeaderBillingId(Long billingId);
}