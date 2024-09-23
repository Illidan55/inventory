package org.scottishfolds.repository;

import org.scottishfolds.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'type': { '$regex': ?0, '$options': 'i' } } ] }")
    Page<Product> findByKeyword(String keyword, Pageable pageable);
}
