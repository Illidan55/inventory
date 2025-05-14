package org.scottishfolds.service;

import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.entity.Sale;
import org.scottishfolds.repository.SaleRepository;
import org.scottishfolds.requestDTO.CreateSale;
import org.scottishfolds.requestDTO.EditSale;
import org.scottishfolds.utility.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for sale controller
 */
@Service
@Slf4j
public class SaleService {
    private final SaleRepository saleRepository;


    /**
     * Constructor dependency injection for SaleRepository
     *
     * @param saleRepository
     */
    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public enum SaleTimeFrame {
        PAST_DAY,
        PAST_WEEK,
        PAST_MONTH,
        PAST_3_MONTHS,
        PAST_6_MONTHS,
        PAST_YEAR
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
        Sale sale = new Sale(null,
                DateUtils.converStringToInstant(createSale.getSaleDate()),
                createSale.getName(),
                createSale.getType(),
                createSale.getCount(),
                createSale.getLocation(),
                createSale.getCost(),
                createSale.getSalePrice());
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
    public Page<Sale> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return saleRepository.findByKeyword(keyword, pageable);
    }

    public Sale generateSaleEntity(EditSale editSale) {
        return new Sale(editSale.getId(),
                DateUtils.converStringToInstant(editSale.getSaleDate()),
                editSale.getName(),
                editSale.getType(),
                editSale.getCount(),
                editSale.getLocation(),
                editSale.getCost(),
                editSale.getSalePrice());
    }
    public Map<String, Object> findSalesInPastTimeFrame() {
        Instant endDate = Instant.now();
        Instant startDate;
        ZonedDateTime nowInUtc = ZonedDateTime.ofInstant(endDate, ZoneOffset.UTC);
        SaleTimeFrame timeFrame = SaleTimeFrame.PAST_6_MONTHS;

        switch (timeFrame) {
            case PAST_DAY:
                startDate = endDate.minus(1, ChronoUnit.DAYS);
                break;
            case PAST_WEEK:
                startDate = endDate.minus(1, ChronoUnit.WEEKS);
                break;
            case PAST_MONTH:
                startDate = nowInUtc.minusMonths(1).toInstant();
                break;
            case PAST_3_MONTHS:
                startDate = nowInUtc.minusMonths(3).toInstant();
                break;
            case PAST_6_MONTHS:
                startDate = nowInUtc.minusMonths(6).toInstant();
                break;
            case PAST_YEAR:
                startDate = nowInUtc.minusYears(1).toInstant();
                break;
            default:
                throw new IllegalArgumentException("Unsupported time frame: " + timeFrame);
        }

        List<String> labels = List.of("January", "February", "March", "April", "May", "June");
        List<String> salesValues = List.of("100", "200", "300", "400", "500", "600");

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("chartLabels", labels);
        chartData.put("chartSalesData", salesValues);
        chartData.put("chartTitle", "Last 6 Month Sales");
        return chartData;
    }
}
