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
        public String period;
        public String location;
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
        ZonedDateTime startDateZonedLoop;

        SaleTimeFrame timeFrame = SaleTimeFrame.valueOf(timeFrameString.toUpperCase());
        String mongoGroupDateFormat;
        ChronoUnit iterationUnit;
        DateTimeFormatter javaPeriodFormatter;


        ZonedDateTime startOfTodayZoned = nowZoned.truncatedTo(ChronoUnit.DAYS);

        switch (timeFrame) {
            case PAST_WEEK:
                startDateZonedLoop = startOfTodayZoned.minusDays(6);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_MONTH:
                startDateZonedLoop = startOfTodayZoned.minusDays(29);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_3_MONTHS:
                startDateZonedLoop = startOfTodayZoned.minusMonths(2).withDayOfMonth(1);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_6_MONTHS:
                startDateZonedLoop = startOfTodayZoned.minusMonths(5).withDayOfMonth(1);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_YEAR:
                startDateZonedLoop = startOfTodayZoned.minusMonths(11).withDayOfMonth(1);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%Y";
                iterationUnit = ChronoUnit.MONTHS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            default:
                log.warn("Unsupported time frame: {}, defaulting to PAST_WEEK", timeFrameString);
                timeFrame = SaleTimeFrame.PAST_WEEK;
                startDateZonedLoop = startOfTodayZoned.minusDays(6);
                startDateInstant = startDateZonedLoop.toInstant();
                mongoGroupDateFormat = "%m-%d-%Y";
                iterationUnit = ChronoUnit.DAYS;
                javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
        }

        // --- Aggregation Pipeline ---
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("saleDate").gte(Date.from(startDateInstant)).lt(endDateExclusive)
        );
        ProjectionOperation projectFieldsForGrouping = Aggregation.project("count", "location")
                .and(DateOperators.DateToString.dateOf("saleDate").toString(mongoGroupDateFormat).withTimezone(DateOperators.Timezone.valueOf("UTC"))).as("period");
        GroupOperation groupOperation = Aggregation.group(Fields.fields("period", "location"))
                .sum("count").as("totalCount");
        ProjectionOperation projectToMatchDTO = Aggregation.project("totalCount")
                .and("_id.period").as("period")
                .and("_id.location").as("location");
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "period", "location");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation, projectFieldsForGrouping, groupOperation, projectToMatchDTO, sortOperation
        );

        AggregationResults<AggregationDataPoint> results = mongoTemplate.aggregate(
                aggregation, Sale.class, AggregationDataPoint.class
        );
        List<AggregationDataPoint> aggregatedDataFromDB = results.getMappedResults();

        // --- Zero-Fill Logic and Data Structuring for Multiple Lines ---
        LinkedHashMap<String, Integer> inStoreSalesMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> onlineSalesMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> totalSalesMap = new LinkedHashMap<>();

        List<String> chartLabels = new ArrayList<>();
        ZonedDateTime currentLoopDate = startDateZonedLoop;

        while (currentLoopDate.toInstant().isBefore(endDateExclusive)) {
            String periodKey = currentLoopDate.format(javaPeriodFormatter);

            chartLabels.add(periodKey);
            inStoreSalesMap.put(periodKey, 0);
            onlineSalesMap.put(periodKey, 0);
            totalSalesMap.put(periodKey, 0);

            currentLoopDate = currentLoopDate.plus(1, iterationUnit);
        }

        // Populate maps with actual sales data from aggregation
        for (AggregationDataPoint point : aggregatedDataFromDB) {
            String period = point.period;
            String location = point.location;
            Integer count = point.totalCount != null ? point.totalCount : 0;

            // Ensure the period from DB data exists in our generated labels/map keys
            if (location != null && period != null && totalSalesMap.containsKey(period)) {
                totalSalesMap.compute(period, (p, currentTotal) -> (currentTotal == null ? 0 : currentTotal) + count);

                if ("In Store".equalsIgnoreCase(location)) {
                    inStoreSalesMap.compute(period, (p, currentLocTotal) -> (currentLocTotal == null ? 0 : currentLocTotal) + count);
                } else if ("Online".equalsIgnoreCase(location)) {
                    onlineSalesMap.compute(period, (p, currentLocTotal) -> (currentLocTotal == null ? 0 : currentLocTotal) + count);
                }
            }
        }

        List<Map<String, Object>> datasets = new ArrayList<>();
        datasets.add(createDatasetForChart("In Store", new ArrayList<>(inStoreSalesMap.values()), "rgb(255, 99, 132)"));
        datasets.add(createDatasetForChart("Online", new ArrayList<>(onlineSalesMap.values()), "rgb(54, 162, 235)"));
        datasets.add(createDatasetForChart("Total", new ArrayList<>(totalSalesMap.values()), "rgb(75, 192, 192)"));

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("chartLabels", chartLabels);
        chartData.put("datasets", datasets);
        chartData.put("chartTitle", "Sales Comparison - " + timeFrame.toString().replace("_", " ").toLowerCase());

        log.info("Chart data for {}: {} labels (zero-filled). Range: {} to {}. MongoFormat: {}",
                timeFrameString, chartLabels.size(), startDateInstant, endDateExclusive, mongoGroupDateFormat);
        return chartData;
    }

    // Helper method to create a dataset map structure for Chart.js
    private Map<String, Object> createDatasetForChart(String label, List<Integer> data, String borderColor) {
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", label);
        dataset.put("data", data);
        dataset.put("fill", false);
        dataset.put("borderColor", borderColor);
        dataset.put("tension", 0.1);
        dataset.put("borderWidth", 2);
        return dataset;
    }
}

