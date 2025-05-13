package org.scottishfolds.controller;


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
public class SalesController {


    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/") // Changed from @RequestMapping
    public String homePage(@RequestParam(defaultValue = "1") int pageNumber,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "name") String sortField,
                           @RequestParam(defaultValue = "asc") String sortDirection,
                           Model model) {
        // Directly call getPage, keyword is null here
        return getPage(pageNumber, pageSize, sortField, sortDirection, null, model);
    }

    @PostMapping("/addSale")
    public String createSale(@ModelAttribute("createSale") CreateSale createSale) {
        saleService.createSale(createSale);
        return "redirect:/sales/";
    }

    @PostMapping("/editSale")
    public String editSale(EditSale editSale) {
        Sale sale = saleService.generateSaleEntity(editSale);
        saleService.save(sale);
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
                          @RequestParam(defaultValue = "name") String sortField,
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
        model.addAttribute("sales", pageResult.getContent());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", "sales");


        return "sales";
    }

    @GetMapping("/saleData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSaleData() {
        List<String> labels = Arrays.asList("January", "February", "March", "April", "May", "June");
        List<Integer> salesValues = Arrays.asList(150, 0, 180, 250, 200, 300);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("chartLabels", labels);
        chartData.put("chartSalesData", salesValues);
        chartData.put("chartTitle", "Monthly Sales Performance");

        return ResponseEntity.ok(chartData);
    }
}
