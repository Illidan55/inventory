package org.scottishfolds.controller;


import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.entity.Sale;
import org.scottishfolds.requestDTO.CreateSale;
import org.scottishfolds.requestDTO.EditSale;
import org.scottishfolds.service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Sales controller to handle the backend for sales
 */
@Controller
@RequestMapping("/sales")
@Slf4j
public class SalesController {


    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/") // Changed from @RequestMapping
    public String homePage(@RequestParam(defaultValue = "1") int pageNumber,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "saleDate") String sortField,
                           @RequestParam(defaultValue = "asc") String sortDirection,
                           Model model) {
        return getPage(pageNumber, pageSize, sortField, sortDirection, null, model);
    }

    @PostMapping("/addSale")
    public String createSale(@ModelAttribute("createSale") CreateSale createSale) {
        saleService.createSale(createSale);
        return "redirect:/sales/";
    }

    @PostMapping("/editSale")
    public String editSale(EditSale editSale) {
        saleService.updateSale(editSale);
        return "redirect:/sales/";
    }

    @GetMapping("/getSale/{id}")
    @ResponseBody
    public Optional<Sale> findById(@PathVariable(name = "id") String id) {
        return saleService.findById(id);
    }

    @PostMapping("/deleteSale")
    public String deleteSale(@RequestParam String id) {
        saleService.deleteById(id);
        return "redirect:/sales/";
    }

    @GetMapping("/page")
    public String getPage(@RequestParam(defaultValue = "1") int pageNumber,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(defaultValue = "saleDate") String sortField,
                          @RequestParam(defaultValue = "asc") String sortDirection,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        Page<Sale> pageResult;

        if (keyword != null && !keyword.isEmpty()) {
            pageResult = saleService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            pageResult = saleService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Sale> sales = pageResult.getContent();
        model.addAttribute("page", pageResult);
        model.addAttribute("sales", sales);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", "sales");


        return "sales";
    }

    @GetMapping("/saleChart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSaleChartData(@RequestParam String timeframe) {
        try {
            SaleService.SaleTimeFrame.valueOf(timeframe.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid timeframe value"));
        }
        Map<String, Object> chartData = saleService.getSalesDataForLineChart(timeframe);
        return ResponseEntity.ok(chartData);
    }
    @GetMapping("/revenueChart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueChartData(@RequestParam String timeframe) {
        try {
            SaleService.SaleTimeFrame.valueOf(timeframe.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid timeframe value: " + timeframe));
        }
        Map<String, Object> chartData = saleService.getRevenueDataForLineChart(timeframe);
        return ResponseEntity.ok(chartData);
    }
}
