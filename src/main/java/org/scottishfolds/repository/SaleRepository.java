package org.scottishfolds.repository;

import org.scottishfolds.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Mongo repository for products that allows searching multiple columns
 */
@Repository
public interface SaleRepository extends MongoRepository<Sale, String> {
    @Query("""
            { '$or': [ 
                        { 'name': { '$regex': ?0, '$options': 'i' } }, 
                        { 'type': { '$regex': ?0, '$options': 'i' } },
                        { 'location': { '$regex': ?0, '$options': 'i' } }
                    ] 
            }
                        """)
    Page<Sale> findByKeyword(String keyword, Pageable pageable);

    List<Sale> findBySaleDateBetweenAndLocation(Instant startDate, Instant endDate, String location);
}

