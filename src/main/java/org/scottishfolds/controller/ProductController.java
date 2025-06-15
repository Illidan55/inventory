package org.scottishfolds.controller;


import org.scottishfolds.entity.Product;
import org.scottishfolds.requestDTO.CreateProduct;
import org.scottishfolds.service.ProductService;
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
    public String editProduct(Product product,
                           @RequestParam("pageNumber") int pageNumber,
                           @RequestParam("pageSize") int pageSize,
                           @RequestParam("sortField") String sortField,
                           @RequestParam("sortDirection") String sortDirection,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           RedirectAttributes redirectAttributes) {
        productService.save(product);

        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("sortField", sortField);
        redirectAttributes.addAttribute("sortDirection", sortDirection);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/product/page";
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
        Page<Product> pageResult;

        if (keyword != null && !keyword.isEmpty()) {
            pageResult = productService.findByKeyWord(pageNumber, pageSize, sortField, sortDirection, keyword);
        } else {
            pageResult = productService.findAll(pageNumber, pageSize, sortField, sortDirection);
        }

        List<Product> products = pageResult.getContent();
        model.addAttribute("page", pageResult);
        model.addAttribute("products", products);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", "product");


        return "product";
    }
    /**
     * Controller method to handle CSV file upload for importing products.
     *
     * @param file The uploaded CSV file.
     * @param redirectAttributes Used to add flash attributes for messages on redirect.
     * @return A redirect string to the home page.
     */
    @PostMapping("/import")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/product/";
        }
        try {
            productService.importProductsFromCSV(file);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported products from " + file.getOriginalFilename());
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to import products: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/product/";

    }
}
