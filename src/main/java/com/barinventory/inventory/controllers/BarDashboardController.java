package com.barinventory.inventory.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bar")
public class BarDashboardController {

	@GetMapping("/dashboard")
	public String dashboard() {
		return "bar/dashboard";
	}
}