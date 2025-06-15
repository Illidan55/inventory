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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public String editSale(EditSale editSale,
                           @RequestParam("pageNumber") int pageNumber,
                           @RequestParam("pageSize") int pageSize,
                           @RequestParam("sortField") String sortField,
                           @RequestParam("sortDirection") String sortDirection,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           RedirectAttributes redirectAttributes) {
        saleService.updateSale(editSale);

        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("sortField", sortField);
        redirectAttributes.addAttribute("sortDirection", sortDirection);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/sales/page";
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
    /**
     * Controller method to handle CSV file upload for importing sales.
     *
     * @param file               The uploaded CSV file.
     * @param redirectAttributes Used to add flash attributes for messages on redirect.
     * @return A redirect string to the home page.
     */
    @PostMapping("/import")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/sales/";
        }
        try {
            saleService.importSalesFromCSV(file);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported sales from " + file.getOriginalFilename());
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to import sales: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/sales/";

    }
}
