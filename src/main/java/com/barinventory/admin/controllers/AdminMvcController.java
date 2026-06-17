package com.barinventory.admin.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barinventory.admin.dtos.BarSummaryResponse;
import com.barinventory.admin.dtos.EditBarRequest;
import com.barinventory.admin.dtos.EditSubscriptionRequest;
import com.barinventory.admin.dtos.OnboardStoreRequest;
import com.barinventory.admin.services.AdminBarService;
import com.barinventory.admin.services.AdminDashboardService;
import com.barinventory.admin.services.AdminOnboardingService;
import com.barinventory.admin.services.DepotCatalogService;
import com.barinventory.admin.services.DepotCategoryService;
import com.barinventory.admin.services.DepotDistributorService;
import com.barinventory.admin.services.DepotManufacturerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMvcController {

    private final AdminOnboardingService onboardingService;
    private final AdminBarService barService;
    private final AdminDashboardService dashboardService;
    private final DepotCategoryService categoryService;
    private final DepotManufacturerService manufacturerService;
    private final DepotDistributorService distributorService;
    private final DepotCatalogService catalogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("onboardRequest", new OnboardStoreRequest());
        model.addAttribute("bars", barService.getAllBars());
        model.addAttribute("stats", dashboardService.getDashboardStats());

        // File location:
        // src/main/resources/templates/admin/dashboard.html
        return "admin/dashboard";
    }

    @PostMapping("/onboard")
    public String onboard(@ModelAttribute OnboardStoreRequest request, RedirectAttributes ra) {

        onboardingService.onboardNewStore(request);

        ra.addAttribute("success", true);

        return "redirect:/admin/dashboard";
    }

    @PatchMapping("/bars/{barId}")
    @ResponseBody
    public BarSummaryResponse editBar(@PathVariable Long barId, @RequestBody EditBarRequest request) {

        return barService.editBar(barId, request);
    }

    @PatchMapping("/bars/{barId}/subscription")
    @ResponseBody
    public void editSubscription(@PathVariable Long barId, @RequestBody EditSubscriptionRequest request) {

        barService.editSubscription(barId, request);
    }
    
    
   /* @GetMapping("/catalog/brands/new")
    public String newBrandPage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("manufacturers", manufacturerService.getAll());
        model.addAttribute("distributors", distributorService.getAll());
        return "admin/admin-brand-form";
    }

    @GetMapping("/catalog/tree")
    public String catalogTreePage(Model model) {
        model.addAttribute("catalog", catalogService.getFullCatalogTree());
        return "admin/admin-catalog-tree";
    }*/

    @GetMapping("/catalog/brands/new")
    public String newBrandPage() {

        return "admin/admin-brand-form";
    }

    @GetMapping("/catalog/tree")
    public String catalogTreePage() {

        return "admin/admin-catalog-tree";
    }
    
    @GetMapping("/opening-stock")
    public String openingStockPage(@RequestParam Long barId, Model model) {
        model.addAttribute("bar", barService.getBarById(barId));
        return "admin/admin-opening-stock";
    }
    
}