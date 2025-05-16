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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.*;

/**
 * Service for sale controller
 */
@Service
@Slf4j
public class SaleService {
    private final SaleRepository saleRepository;
    private final MongoTemplate mongoTemplate;


    /**
     * Constructor dependency injection for SaleRepository
     *
     * @param saleRepository
     */
    public SaleService(SaleRepository saleRepository, MongoTemplate mongoTemplate) {
        this.saleRepository = saleRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public enum SaleTimeFrame {
        PAST_DAY,
        PAST_WEEK,
        PAST_MONTH,
        PAST_3_MONTHS,
        PAST_6_MONTHS,
        PAST_YEAR
    }

    private static class AggregationDataPoint {
        public String period; // This will be the "_id" from grouping (e.g., "2023-10-26")
        public Integer totalCount;
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

    /**
     * Fetches and aggregates sales data for a line chart based on the specified timeframe.
     *
     * @param timeFrameString The timeframe string (e.g., "PAST_WEEK", "PAST_MONTH").
     * @return A map containing chartLabels, chartSalesData, and chartTitle.
     */

    public Map<String, Object> getSalesDataForLineChart(String timeFrameString) {
        Instant now = Instant.now();
        ZonedDateTime nowZoned = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);

        Instant endDateExclusive = nowZoned.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Instant startDateInstant;
        SaleTimeFrame timeFrame = SaleTimeFrame.valueOf(timeFrameString.toUpperCase());
        String mongoGroupDateFormat;
        ChronoUnit iterationUnit;
        DateTimeFormatter javaPeriodFormatter;

        ZonedDateTime startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS);

        switch (timeFrame) {
            case PAST_WEEK:
                startDateZoned = startDateZoned.minusDays(6);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_MONTH:
                startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS).minusDays(29);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_3_MONTHS:
                startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS).minusMonths(2).withDayOfMonth(1);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_6_MONTHS:
                startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS).minusMonths(5).withDayOfMonth(1);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_YEAR:
                startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS).minusMonths(11).withDayOfMonth(1);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            default:
                log.warn("Unsupported time frame: {}, defaulting to PAST_WEEK", timeFrameString);
                timeFrame = SaleTimeFrame.PAST_WEEK;
                startDateZoned = nowZoned.truncatedTo(ChronoUnit.DAYS).minusDays(6);
                startDateInstant = startDateZoned.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
        }

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("saleDate").gte(Date.from(startDateInstant)).lt(endDateExclusive)
        );
        ProjectionOperation projectDateForGrouping = Aggregation.project("count")
                .and(DateOperators.DateToString.dateOf("saleDate").toString(mongoGroupDateFormat).withTimezone(DateOperators.Timezone.valueOf("UTC"))).as("groupingPeriod");
        GroupOperation groupOperation = Aggregation.group("groupingPeriod")
                .sum("count").as("totalCount");
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "_id");
        ProjectionOperation projectToMatchDTO = Aggregation.project("totalCount").and("_id").as("period");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation, projectDateForGrouping, groupOperation, sortOperation, projectToMatchDTO
        );

        AggregationResults<AggregationDataPoint> results = mongoTemplate.aggregate(
                aggregation, Sale.class, AggregationDataPoint.class
        );
        List<AggregationDataPoint> aggregatedDataFromDB = results.getMappedResults();


        LinkedHashMap<String, Integer> salesByPeriodMap = new LinkedHashMap<>();
        ZonedDateTime currentLoopDate = startDateZoned; // Start iterating from the calculated ZonedDateTime start

        while (currentLoopDate.toInstant().isBefore(endDateExclusive)) {
            String periodKey = currentLoopDate.format(javaPeriodFormatter);
            salesByPeriodMap.put(periodKey, 0);

            currentLoopDate = currentLoopDate.plus(1, iterationUnit);
        }

        for (AggregationDataPoint point : aggregatedDataFromDB) {
            if (point.period != null) {
                salesByPeriodMap.put(point.period, point.totalCount);
            }
        }

        List<String> chartLabels = new ArrayList<>(salesByPeriodMap.keySet());
        List<Integer> chartSalesData = new ArrayList<>(salesByPeriodMap.values());

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("chartLabels", chartLabels);
        chartData.put("chartSalesData", chartSalesData);
        chartData.put("chartTitle", "Sales - " + timeFrame.toString().replace("_", " ").toLowerCase());

        return chartData;
    }
}

