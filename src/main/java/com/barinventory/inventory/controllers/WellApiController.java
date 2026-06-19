package com.barinventory.inventory.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barinventory.config.SecurityUtils;
import com.barinventory.inventory.repos.WellRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wells")
@RequiredArgsConstructor
public class WellApiController {

	private final WellRepository wellRepository;

	@GetMapping("/my/count")
	public long myWellsCount() {
		Long barId = SecurityUtils.getBarId();
		return wellRepository.countByBarIdAndActiveTrue(barId);
	}
}