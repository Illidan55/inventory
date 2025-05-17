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
     * <p>
     * Note: Returns getPage with default parameters
     *
     * @param model
     * @return
     */

    @GetMapping("/")
    public String homePage(@RequestParam(defaultValue = "1") int pageNumber,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "name") String sortField,
                           @RequestParam(defaultValue = "asc") String sortDirection,
                           Model model) {
        return getPage(pageNumber, pageSize, sortField, sortDirection, null, model);

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
    @PostMapping(value = "/editItem")
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
    @GetMapping(value = "/getItem/{id}")
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
    @PostMapping(value = "/deleteItem")
    public String deleteItem(String id) {
        itemService.deleteById(id);
        return "redirect:/";
    }

    /**
     * Method to return the model to thymeleaf.
     * <p>
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
        model.addAttribute("page", pageResult);
        model.addAttribute("items", items);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", "material");


        return "index";
    }
}
