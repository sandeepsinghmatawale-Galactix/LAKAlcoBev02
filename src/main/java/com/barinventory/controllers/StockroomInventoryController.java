package com.barinventory.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.barinventory.config.SecurityUtils;
import com.barinventory.dtos.StockroomClosingRequest;
import com.barinventory.entities.StockroomInventory;
import com.barinventory.services.StockroomInventoryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stockroom")
public class StockroomInventoryController {

    private final StockroomInventoryService stockroomService;

    /*
     -----------------------------------------
     STOCKROOM PAGE
     -----------------------------------------
    */
    @GetMapping("/{sessionId}")
    public String stockroomPage(
            @PathVariable Long sessionId,
            Model model
    ) {

        Long barId = SecurityUtils.getBarId();

        List<StockroomInventory> stocks =
                stockroomService
                        .getStockroomByBarAndSession(
                                barId,
                                sessionId
                        );

        model.addAttribute("stocks", stocks);

        model.addAttribute("sessionId", sessionId);

        model.addAttribute("barId", barId);

        return "stockroom/stockroom-inventory";
    }

    /*
     -----------------------------------------
     UPDATE CLOSING
     -----------------------------------------
    */
    @PostMapping("/closing/{sessionId}")
    public String updateClosing(

            @PathVariable Long sessionId,

            @RequestParam List<Long> brandSizeId,

            @RequestParam List<Integer> closingStock
    ) {

        Long barId = SecurityUtils.getBarId();

        List<StockroomClosingRequest> requests =
                new ArrayList<>();

        for (int i = 0; i < brandSizeId.size(); i++) {

            StockroomClosingRequest req =
                    new StockroomClosingRequest();

            req.setBrandSizeId(
                    brandSizeId.get(i)
            );

            req.setClosingStock(
                    closingStock.get(i)
            );

            requests.add(req);
        }

        stockroomService.updateStockroomClosing(
                barId,
                sessionId,
                requests
        );

        return "redirect:/distribution/create-page/"
                + sessionId;
    }
}