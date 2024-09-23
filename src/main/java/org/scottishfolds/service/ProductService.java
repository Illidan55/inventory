package org.scottishfolds.service;

import org.scottishfolds.entity.Item;
import org.scottishfolds.entity.Product;
import org.scottishfolds.repository.ItemRepository;
import org.scottishfolds.repository.ProductRepository;
import org.scottishfolds.requestDTO.CreateItem;
import org.scottishfolds.requestDTO.CreateProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

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

    public void save(Product product) {
        productRepository.save(product);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    public Page<Product> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return productRepository.findAll(pageable);
    }

    public Page<Product> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return productRepository.findByKeyword(keyword, pageable);
    }
}
