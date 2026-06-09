package com.barinventory.admin.controllers;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.barinventory.admin.dtos.DashboardStatsDTO;
import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.admin.services.AdminDashboardService;
import com.barinventory.admin.services.AdminOnboardingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMvcController {

    private final AdminOnboardingService onboardingService;
    private final AdminDashboardService dashboardService; // ✅ Inject the stats service

    // 🚨 ADD THIS MISSING METHOD TO FIX THE 404 ERROR 🚨
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("onboardRequest", new OnboardStoreRequest());
        return "admin/dashboard"; // Renders templates/admin/dashboard.html
    }

    @GetMapping("/onboard")
    public String showOnboardForm(Model model) {
        model.addAttribute("onboardRequest", new OnboardStoreRequest());
        return "admin/onboard";
    }

    @PostMapping("/onboard")
    public String submitOnboardForm(@ModelAttribute("onboardRequest") OnboardStoreRequest request) {
        onboardingService.onboardNewStore(request);
        return "redirect:/admin/dashboard?success=true";
    }
}