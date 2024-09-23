package org.scottishfolds.controller;


import org.scottishfolds.entity.Item;
import org.scottishfolds.entity.Product;
import org.scottishfolds.requestDTO.CreateItem;
import org.scottishfolds.requestDTO.CreateProduct;
import org.scottishfolds.service.ItemService;
import org.scottishfolds.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/product/")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping("/")
    public String homePage(Model model) {
        return getPage(1, 10, "name", "asc", null, model);

    }

    @PostMapping("/addProduct")
    public String createProduct(@ModelAttribute("createItem") CreateProduct createProduct) {
        productService.createProduct(createProduct);
        return "redirect:/product/";
    }

    @RequestMapping(value = "/editProduct", method = {RequestMethod.PUT, RequestMethod.GET})
    public String editProduct(Product product) {
        productService.save(product);
        return "redirect:/product/";
    }

    @RequestMapping(value = "/getProduct/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Optional<Product> findById(@PathVariable(name = "id") String id) {
        return productService.findById(id);
    }

    @RequestMapping(value = "/deleteProduct", method = {RequestMethod.DELETE, RequestMethod.GET})
    public String deleteProduct(String id) {
        productService.deleteById(id);
        return "redirect:/product/";
    }

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
