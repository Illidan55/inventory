package org.scottishfolds.service;

import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.entity.Product;
import org.scottishfolds.repository.ProductRepository;
import org.scottishfolds.requestDTO.CreateProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for product controller
 */
@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * Constructor dependency injection for ProductRepository
     *
     * @param productRepository
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Find a product by id
     *
     * @param id
     * @return
     */
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    /**
     * Create a new product using the CreateProduct dto
     *
     * @param createProduct
     */
    public void createProduct(CreateProduct createProduct) {
        Product product = new Product();
        product.setName(createProduct.getName());
        product.setType(createProduct.getType());
        product.setBackStock(createProduct.getBackStock());
        product.setOnlinePrice(createProduct.getOnlinePrice());
        product.setInStoreStock(createProduct.getInStoreStock());
        product.setInStorePrice(createProduct.getInStorePrice());
        product.setOnlineStock(createProduct.getOnlineStock());
        productRepository.save(product);

        productRepository.save(product);
    }

    /**
     * Save product
     *
     * @param product
     */
    public void save(Product product) {
        productRepository.save(product);
    }

    /**
     * Delete product by id
     *
     * @param id
     */
    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    /**
     * Find a page based on page, size, sortField, sortDirection
     *
     * @param page
     * @param size
     * @param sortField
     * @param sortDirection
     * @return
     */
    public Page<Product> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return productRepository.findAll(pageable);
    }

    /**
     * Find a page based on page, size, sortField, sortDirection and keyword
     * <p>
     * Note: Will search both type and name columns
     *
     * @param page
     * @param size
     * @param sortField
     * @param sortDirection
     * @param keyword
     * @return
     */
    public Page<Product> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return productRepository.findByKeyword(keyword, pageable);
    }
    /**
     * Imports products from a CSV file.
     * Assumes CSV format: name, type, backStock, inStoreStock, onlineStock, inStorePrice, onlinePrice
     *
     * @param file The CSV file to import.
     * @throws IOException If an error occurs during file reading.
     * @throws IllegalArgumentException If the file data is not as expected.
     */
    public void importProductsFromCSV(MultipartFile file) throws IOException {
        List<Product> productsToSave = new ArrayList<>();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length == 7) {
                    Product product = new Product();
                    product.setName(values[0].trim());
                    product.setType(values[1].trim());
                    product.setBackStock(Integer.parseInt(values[2].trim()));
                    product.setInStoreStock(Integer.parseInt(values[3].trim()));
                    product.setOnlineStock(Integer.parseInt(values[4].trim()));

                    try {
                        String inStorePriceStr = values[5].trim();
                        if(inStorePriceStr.contains("$")) {
                            inStorePriceStr = inStorePriceStr.replace("$", "");
                        }
                        product.setInStorePrice(Float.parseFloat(inStorePriceStr));
                        String onlinePriceStr = values[6].trim();
                        if(onlinePriceStr.contains("$")) {
                            onlinePriceStr = onlinePriceStr.replace("$", "");
                        }
                        product.setOnlinePrice(Float.parseFloat(onlinePriceStr));
                    } catch (NumberFormatException e) {
                        log.error("Skipping row due to invalid float format: {}. Error: {}", line, e.getMessage());
                        continue;
                    }
                    productsToSave.add(product);
                } else {
                    log.error("Skipping malformed CSV row: {}", line);
                }
            }
        }

        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);

        }
    }
}
