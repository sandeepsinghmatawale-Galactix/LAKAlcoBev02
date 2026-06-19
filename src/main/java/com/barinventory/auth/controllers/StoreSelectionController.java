package com.barinventory.auth.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.barinventory.auth.entities.Bar;
import com.barinventory.auth.entities.UserBarAccess;
import com.barinventory.auth.repos.BarRepository;
import com.barinventory.auth.repos.UserBarAccessRepository;
import com.barinventory.auth.servcies.CustomUserDetails;
import com.barinventory.config.SecurityUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StoreSelectionController {

	private final UserBarAccessRepository userBarAccessRepository;
	private final BarRepository barRepository;

	@GetMapping("/select-store")
	public String selectStorePage(Model model) {

		CustomUserDetails user = SecurityUtils.getCurrentUser();

		List<UserBarAccess> accesses = userBarAccessRepository.findByUserIdAndActiveTrue(user.getUserId());

		List<Bar> bars = accesses.stream().map(access -> barRepository.findById(access.getBarId()).orElse(null))
				.filter(bar -> bar != null).toList();

		model.addAttribute("bars", bars);

		return "auth/select-store";
	}

	@PostMapping("/select-store")
	public String selectStore(@RequestParam Long barId, HttpSession session) {

	    CustomUserDetails user = SecurityUtils.getCurrentUser();

	    userBarAccessRepository.findByUserIdAndBarIdAndActiveTrue(user.getUserId(), barId)
	            .orElseThrow(() -> new RuntimeException("You do not have access to this store."));

	    session.setAttribute(SecurityUtils.SELECTED_BAR_ID, barId);

	    return "redirect:/bar/dashboard";
	}
}