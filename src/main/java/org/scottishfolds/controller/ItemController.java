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
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @RequestMapping("/")
    public String homePage(Model model) {
        return getPage(1, 10, "name", "asc",model);

    }

    @PostMapping("/addItem")
    public String createItem(@ModelAttribute("createItem") CreateItem createItem) {
        itemService.createItem(createItem);
        return "redirect:/";
    }

    @RequestMapping(value = "/editItem", method = {RequestMethod.PUT, RequestMethod.GET})
    public String editItem(Item item) {
        itemService.save(item);
        return "redirect:/";
    }
    @RequestMapping(value = "/getItem/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Optional<Item> findById(@PathVariable(name = "id") String id) {
        return itemService.findById(id);
    }
    @RequestMapping(value = "/deleteItem", method = {RequestMethod.DELETE, RequestMethod.GET})
    public String editItem(String id) {
        itemService.deleteById(id);
        return "redirect:/";
    }
    @GetMapping("/page")
    public String getPage(@RequestParam int pageNumber,
                          @RequestParam int pageSize,
                          @RequestParam String sortField,
                          @RequestParam String sortDirection,
                          Model model){

        Page<Item> page = itemService.findByPage(pageNumber, pageSize, sortField, sortDirection);
        List<Item> items = page.getContent();
        model.addAttribute("items", items);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSort", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        return "index";
    }
}
