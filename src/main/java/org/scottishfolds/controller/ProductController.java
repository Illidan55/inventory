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
@RequestMapping("/product/")
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
    @RequestMapping("/")
    public String homePage(Model model) {
        return getPage(1, 10, "name", "asc", null, model);

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
    @RequestMapping(value = "/editProduct", method = {RequestMethod.PUT, RequestMethod.GET})
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
    @RequestMapping(value = "/getProduct/{id}", method = RequestMethod.GET)
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
    @RequestMapping(value = "/deleteProduct", method = {RequestMethod.DELETE, RequestMethod.GET})
    public String deleteProduct(String id) {
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
    public String getPage(@RequestParam int pageNumber,
                          @RequestParam int pageSize,
                          @RequestParam String sortField,
                          @RequestParam String sortDirection,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        Page<Product> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = productService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            page = productService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Product> products = page.getContent();
        model.addAttribute("products", products);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortField", sortField);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSort", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());


        return "product";
    }
}
