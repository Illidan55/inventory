package org.scottishfolds.controller;


import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.entity.Item;
import org.scottishfolds.requestDTO.CreateItem;
import org.scottishfolds.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Item controller to handle the backend for the raw materials
 */
@Controller
@Slf4j
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
    public String createItem(@ModelAttribute("createItem") CreateItem createItem,
                             @RequestParam("pageNumber") int pageNumber,
                             @RequestParam("pageSize") int pageSize,
                             @RequestParam("sortField") String sortField,
                             @RequestParam("sortDirection") String sortDirection,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             RedirectAttributes redirectAttributes) {
        itemService.createItem(createItem);
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("sortField", sortField);
        redirectAttributes.addAttribute("sortDirection", sortDirection);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/page";
    }

    /**
     * Mapping for thymeleaf to edit item
     *
     * @param item
     * @return
     */
    @PostMapping(value = "/editItem")
    public String editItem(Item item,
                           @RequestParam("pageNumber") int pageNumber,
                           @RequestParam("pageSize") int pageSize,
                           @RequestParam("sortField") String sortField,
                           @RequestParam("sortDirection") String sortDirection,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           RedirectAttributes redirectAttributes) {
        itemService.save(item);

        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("sortField", sortField);
        redirectAttributes.addAttribute("sortDirection", sortDirection);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/page";
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
     * Controller method to handle CSV file upload for importing items.
     *
     * @param file The uploaded CSV file.
     * @param redirectAttributes Used to add flash attributes for messages on redirect.
     * @return A redirect string to the home page.
     */
    @PostMapping("/import")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/";
        }
        try {
            itemService.importItemsFromCSV(file);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported items from " + file.getOriginalFilename());
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to import items: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/";

    }

    /**
     * Mapping for thymeleaf to delete item by id
     *
     * @param id
     * @return
     */
    @PostMapping(value = "/deleteItem")
    public String deleteItem(String id,
                             @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                             @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                             @RequestParam("sortField") String sortField,
                             @RequestParam("sortDirection") String sortDirection,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             RedirectAttributes redirectAttributes) {
        itemService.deleteById(id);
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("sortField", sortField);
        redirectAttributes.addAttribute("sortDirection", sortDirection);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/page";
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
