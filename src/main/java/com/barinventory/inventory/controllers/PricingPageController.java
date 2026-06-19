package com.barinventory.inventory.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pricing")
public class PricingPageController {

	@GetMapping
	public String pricingPage() {
		return "pricing/bar-product-pricing";
	}
}