package org.scottishfolds.controller;


import org.scottishfolds.entity.Product;
import org.scottishfolds.requestDTO.CreateProduct;
import org.scottishfolds.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Product Controller to handle the backend for product inventory
 */
@Controller
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    /**
     * Constructor dependency injection for ProductService
     *
     * @param productService
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Mapping for homepage
     *
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
        // Directly call getPage, keyword is null here
        return getPage(pageNumber, pageSize, sortField, sortDirection, null, model);

    }

    /**
     * Mapping for thymeleaf to create product
     *
     * @param createProduct
     * @return
     */
    @PostMapping("/addProduct")
    public String createProduct(@ModelAttribute("createItem") CreateProduct createProduct) {
        productService.createProduct(createProduct);
        return "redirect:/product/";
    }

    /**
     * Mapping for thymeleaf to edit product
     *
     * @param product
     * @return
     */
    @PostMapping(value = "/editProduct")
    public String editProduct(Product product) {
        productService.save(product);
        return "redirect:/product/";
    }

    /**
     * Mapping for thymeleaf to get product using id
     *
     * @param id
     * @return
     */
    @GetMapping("/getProduct/{id}")
    @ResponseBody
    public Optional<Product> findById(@PathVariable(name = "id") String id) {
        return productService.findById(id);
    }

    /**
     * Mapping for thymeleaf to delete product by id
     *
     * @param id
     * @return
     */
    @PostMapping("/deleteProduct")
    public String deleteProduct(@RequestParam String id) {
        productService.deleteById(id);
        return "redirect:/product/";
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
    public String getPage(@RequestParam(defaultValue = "1") int pageNumber, // Added default values
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(defaultValue = "name") String sortField,
                          @RequestParam(defaultValue = "asc") String sortDirection,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        Page<Product> pageResult;

        if (keyword != null && !keyword.isEmpty()) {
            pageResult = productService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            pageResult = productService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Product> products = pageResult.getContent();
        // 1. Add the Page object itself to the model
        model.addAttribute("page", pageResult); // Use "page" as the attribute name

        // 2. Add the list of items separately (needed for th:each in the table)
        model.addAttribute("products", pageResult.getContent());

        // 3. Add attributes required by the fragment *parameters* that are NOT in the Page object easily
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", "product");


        return "product";
    }
}
