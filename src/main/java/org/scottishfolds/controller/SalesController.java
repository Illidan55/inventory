package org.scottishfolds.controller;


import org.scottishfolds.entity.Item;
import org.scottishfolds.requestDTO.CreateItem;
import org.scottishfolds.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Sales controller to handle the backend for sales
 */
@Controller
@RequestMapping("/sales")
public class SalesController {

    //TODO This is a placeholder controller
    // - Implement sales controller and service and thymeleaf frontend


    private final ItemService itemService;

    public SalesController(ItemService itemService) {
        this.itemService = itemService;
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
    public String createSale(@ModelAttribute("createItem") CreateItem createItem) {
        itemService.createItem(createItem);
        return "redirect:/sales/";
    }

    @PostMapping("/editSale")
    public String editSale(Item item) {
        itemService.save(item);
        return "redirect:/sales/";
    }

    @GetMapping("/getSale/{id}")
    @ResponseBody
    public Optional<Item> findById(@PathVariable(name = "id") String id) {
        return itemService.findById(id);
    }

    @PostMapping("/deleteSale")
    public String deleteSale(@RequestParam String id) {
        itemService.deleteById(id);
        return "redirect:/sales/";
    }

    @GetMapping("/page")
    public String getPage(@RequestParam(defaultValue = "1") int pageNumber, // Added default values
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(defaultValue = "name") String sortField,
                          @RequestParam(defaultValue = "asc") String sortDirection,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        Page<Item> pageResult;

        if (keyword != null && !keyword.isEmpty()) {
            pageResult = itemService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            pageResult = itemService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Item> items = pageResult.getContent();
        // 1. Add the Page object itself to the model
        model.addAttribute("page", pageResult); // Use "page" as the attribute name

        // 2. Add the list of items separately (needed for th:each in the table)
        model.addAttribute("items", pageResult.getContent());

        // 3. Add attributes required by the fragment *parameters* that are NOT in the Page object easily
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");



        return "sales";
    }
}
