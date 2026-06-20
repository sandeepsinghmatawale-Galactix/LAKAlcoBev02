package com.barinventory.billing.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.barinventory.billing.dtos.BillingHeaderResponse;
import com.barinventory.billing.dtos.BillingSaveRequest;
import com.barinventory.billing.services.BillingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

	private final BillingService billingService;

	@GetMapping
	public String billingPage() {
		return "billing/billing-planner";
	}

	@PostMapping("/save")
	@ResponseBody
	public BillingHeaderResponse saveBilling(@RequestBody BillingSaveRequest request) {
		return billingService.saveBilling(request);
	}

	@GetMapping("/history")
	@ResponseBody
	public List<BillingHeaderResponse> history() {
		return billingService.getMyBillingHistory();
	}

	@GetMapping("/{billingId}")
	@ResponseBody
	public BillingHeaderResponse details(@PathVariable Long billingId) {
		return billingService.getMyBillingDetails(billingId);
	}
}