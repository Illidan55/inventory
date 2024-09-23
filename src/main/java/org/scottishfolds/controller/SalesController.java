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

@Controller
@RequestMapping("/sales/")
public class SalesController {
    private final ItemService itemService;

    public SalesController(ItemService itemService) {
        this.itemService = itemService;
    }

    @RequestMapping("/")
    public String homePage(Model model) {
        return getPage(1, 10, "name", "asc", null, model);

    }

    @PostMapping("/addSale")
    public String createSale(@ModelAttribute("createItem") CreateItem createItem) {
        itemService.createItem(createItem);
        return "redirect:/sales/";
    }

    @RequestMapping(value = "/editSale", method = {RequestMethod.PUT, RequestMethod.GET})
    public String editSale(Item item) {
        itemService.save(item);
        return "redirect:/sales/";
    }

    @RequestMapping(value = "/getSale/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Optional<Item> findById(@PathVariable(name = "id") String id) {
        return itemService.findById(id);
    }

    @RequestMapping(value = "/deleteSale", method = {RequestMethod.DELETE, RequestMethod.GET})
    public String deleteSale(String id) {
        itemService.deleteById(id);
        return "redirect:/sales/";
    }

    @GetMapping("/page")
    public String getPage(@RequestParam int pageNumber,
                          @RequestParam int pageSize,
                          @RequestParam String sortField,
                          @RequestParam String sortDirection,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        Page<Item> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = itemService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            page = itemService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Item> items = page.getContent();
        model.addAttribute("items", items);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortField", sortField);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSort", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());



        return "sales";
    }
}
