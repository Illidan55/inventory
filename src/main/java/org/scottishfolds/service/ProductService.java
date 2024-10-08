package org.scottishfolds.service;

import org.scottishfolds.entity.Product;
import org.scottishfolds.repository.ProductRepository;
import org.scottishfolds.requestDTO.CreateProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for product controller
 */
@Service
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
}
