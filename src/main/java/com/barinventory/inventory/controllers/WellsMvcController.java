package com.barinventory.inventory.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.barinventory.config.SecurityUtils;
import com.barinventory.inventory.repos.WellRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/wells")
@RequiredArgsConstructor
public class WellsMvcController {

	private final WellRepository wellRepository;

	@GetMapping
	public String wellsPage(Model model) {

		Long barId = SecurityUtils.getBarId();

		model.addAttribute("wells", wellRepository.findByBarId(barId));

		model.addAttribute("barId", barId);

		return "well/wells-overview";
	}
}