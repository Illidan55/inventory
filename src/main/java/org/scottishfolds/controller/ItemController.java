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
 * Item controller to handle the backend for the raw materials
 */
@Controller
public class ItemController {
    private final ItemService itemService;

    /**
     * Constructor dependency injection for ItemService
     *
     * @param itemService
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Mapping for homepage
     *
     * Note: Returns getPage with default parameters
     *
     * @param model
     * @return
     */
    @RequestMapping("/")
    public String homePage(Model model) {
        return getPage(1, 10, "name", "asc", null, model);

    }

    /**
     * Mapping for thymeleaf to create item
     *
     * @param createItem
     * @return
     */
    @PostMapping("/addItem")
    public String createItem(@ModelAttribute("createItem") CreateItem createItem) {
        itemService.createItem(createItem);
        return "redirect:/";
    }

    /**
     * Mapping for thymeleaf to edit item
     *
     * @param item
     * @return
     */
    @RequestMapping(value = "/editItem", method = {RequestMethod.PUT, RequestMethod.GET})
    public String editItem(Item item) {
        itemService.save(item);
        return "redirect:/";
    }

    /**
     * Mapping for thymeleaf to get item using id
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getItem/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Optional<Item> findById(@PathVariable(name = "id") String id) {
        return itemService.findById(id);
    }

    /**
     * Mapping for thymeleaf to delete item by id
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteItem", method = {RequestMethod.DELETE, RequestMethod.GET})
    public String deleteItem(String id) {
        itemService.deleteById(id);
        return "redirect:/";
    }

    /**
     * Method to return the model to thymeleaf.
     *
     * Allows for paging and sorting of the table
     *
     * @param pageNumber
     * @param pageSize
     * @param sortField
     * @param sortDirection
     * @param keyword
     * @param model
     * @return
     */
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


        return "index";
    }
}
