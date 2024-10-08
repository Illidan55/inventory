package org.scottishfolds.repository;

import org.scottishfolds.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Mongo repository for items that allows searching multiple columns
 */
@Repository
public interface ItemRepository extends MongoRepository<Item, String> {
    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'type': { '$regex': ?0, '$options': 'i' } } ] }")
    Page<Item> findByKeyword(String keyword, Pageable pageable);
}
