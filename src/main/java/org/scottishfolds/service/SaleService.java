package org.scottishfolds.service;

import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.entity.Sale;
import org.scottishfolds.repository.SaleRepository;
import org.scottishfolds.requestDTO.CreateSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service for sale controller
 */
@Service
@Slf4j
public class SaleService {
    private final SaleRepository saleRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    /**
     * Constructor dependency injection for SaleRepository
     *
     * @param saleRepository
     */
    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Find sale by id
     *
     * @param id
     * @return
     */
    public Optional<Sale> findById(String id) {
        return saleRepository.findById(id);
    }

    /**
     * Create sale using the CreateSale dto
     *
     * @param createSale
     */
    public void createSale(CreateSale createSale) {
        Sale sale = new Sale();
        Instant saleInstant = null;
        try{
            if(createSale.getSaleDate() != null && !createSale.getSaleDate().isEmpty()){
                LocalDate localDate = LocalDate.parse(createSale.getSaleDate(), DATE_FORMATTER);
                saleInstant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            }
        }catch (Exception e) {
            log.error("Error parsing date: {}", e.getMessage());
        }
        sale.setSaleDate(saleInstant);
        sale.setName(createSale.getName());
        sale.setType(createSale.getType());
        sale.setCount(createSale.getCount());
        sale.setLocation(createSale.getLocation() );
        sale.setCost(createSale.getCost());
        sale.setSalePrice(createSale.getSalePrice());

        saleRepository.save(sale);

    }

    /**
     * Save sale
     *
     * @param sale
     */
    public void save(Sale sale) {
        saleRepository.save(sale);
    }

    /**
     * Delete sale by id
     *
     * @param id
     */
    public void deleteById(String id) {
        saleRepository.deleteById(id);
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
    public Page<Sale> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return saleRepository.findAll(pageable);
    }

    /**
     * Find a page based on page, size, sortField, sortDirection and keyword
     *
     * Note: Will search both type and name columns
     *
     * @param page
     * @param size
     * @param sortField
     * @param sortDirection
     * @param keyword
     * @return
     */
    public Page<Sale> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return saleRepository.findByKeyword(keyword, pageable);
    }
}
