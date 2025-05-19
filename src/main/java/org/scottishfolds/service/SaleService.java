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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
     * Constructs a SaleService object with the specified SaleRepository and MongoTemplate.
     *
     * @param saleRepository the repository used for managing sale data
     * @param mongoTemplate the MongoTemplate used for database operations
     */
    public SaleService(SaleRepository saleRepository, MongoTemplate mongoTemplate) {
        this.saleRepository = saleRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * The SaleTimeFrame enum represents predefined time frames used
     * for categorizing and analyzing sales data over specific periods.
     * It provides constants for various common durations, such as:
     * - PAST_WEEK: Sales data from the last 7 days.
     * - PAST_MONTH: Sales data from the last 30 days approximately.
     * - PAST_3_MONTHS: Sales data from the last 3 months.
     * - PAST_6_MONTHS: Sales data from the last 6 months.
     * - PAST_YEAR: Sales data from the last 12 months.
     * This enum can be utilized in applications requiring time-based
     * filtering or reporting of sales metrics.
     */
    public enum SaleTimeFrame {
        PAST_WEEK,
        PAST_MONTH,
        PAST_3_MONTHS,
        PAST_6_MONTHS,
        PAST_YEAR
    }

    /**
     * Represents a data point in a sale chart aggregation process. Each instance of this
     * class contains information about a specific period, location and the
     * total count for that data point.
     */
    private static class AggregationDataPoint {
        public String period;
        public String location;
        public Integer totalCount;
    }

    /**
     * Represents a data point in a revenue chart aggregation process. Each instance of this
     * class contains information about a specific period, location and the
     * total count for that data point.
     */
    private static class RevenueDataPoint {
        public String period;
        public String location;
        public Double totalRevenue;
    }

    private static class AggregationInfo {
        public Instant now;
        public ZonedDateTime nowZoned;
        public Instant endDateExclusive;
        public Instant startDateInstant;
        public ZonedDateTime startDateZonedLoop;
        public SaleTimeFrame timeFrame;
        public String mongoGroupDateFormat;
        public ChronoUnit iterationUnit;
        public DateTimeFormatter javaPeriodFormatter;
        public ZonedDateTime startOfTodayZoned;
    }

    /**
     * Finds a sale by its unique identifier.
     *
     * @param id the unique identifier of the sale to find, must not be null
     * @return an {@code Optional} containing the found sale, or an empty {@code Optional} if no sale is found
     */
    public Optional<Sale> findById(String id) {
        return saleRepository.findById(id);
    }

    /**
     * Creates a new sale and stores it in the repository.
     *
     * @param createSale the object containing the details of the sale, including sale date, name, type, count,
     *                   location, cost and sale price
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
     * Updates an existing sale based on the provided EditSale object.
     *
     * @param editSale an object containing the updated sale information
     */
    public void updateSale(EditSale editSale) {
        Sale sale = generateSaleEntity(editSale);
        saleRepository.save(sale);
    }

    /**
     * Deletes an entity with the specified identifier.
     *
     * @param id the unique identifier of the entity to be deleted
     */
    public void deleteById(String id) {
        saleRepository.deleteById(id);
    }

    /**
     * Retrieves a paginated list of sales from the repository based on the provided pagination and sorting parameters.
     *
     * @param page the page number to retrieve (1-indexed)
     * @param size the number of items to retrieve per page
     * @param sortField the field by which to sort the results
     * @param sortDirection the direction of sorting, either "ASC" for ascending or "DESC" for descending
     * @return a paginated list of sales
     */
    public Page<Sale> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return saleRepository.findAll(pageable);
    }

    /**
     * Finds a paginated list of sales that match the given keyword.
     *
     * @param page the page number to retrieve (1-based index)
     * @param size the number of items per page
     * @param sortField the field by which to sort the results
     * @param sortDirection the direction of sorting (ASC for ascending, DESC for descending)
     * @param keyword the keyword to search for
     * @return a Page of sales that match the keyword, sorted, and page
     */
    public Page<Sale> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return saleRepository.findByKeyword(keyword, pageable);
    }

    /**
     * Converts an EditSale object into a Sale entity.
     *
     * @param editSale the EditSale object containing the details to create a Sale entity
     * @return a Sale entity constructed from the provided EditSale object
     */
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

    private void setTimeFrameData(AggregationInfo aggregationInfo, String timeFrame) {
        aggregationInfo.now = Instant.now();
        aggregationInfo.nowZoned = ZonedDateTime.ofInstant(aggregationInfo.now, ZoneOffset.UTC);
        aggregationInfo.endDateExclusive = aggregationInfo.nowZoned.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        aggregationInfo.timeFrame = SaleTimeFrame.valueOf(timeFrame.toUpperCase());
        aggregationInfo.startOfTodayZoned = aggregationInfo.nowZoned.truncatedTo(ChronoUnit.DAYS);

        switch (aggregationInfo.timeFrame) {
            case PAST_WEEK:
                aggregationInfo.startDateZonedLoop = aggregationInfo.startOfTodayZoned.minusDays(6);
                aggregationInfo.startDateInstant = aggregationInfo.startDateZonedLoop.toInstant();
                aggregationInfo.mongoGroupDateFormat = "%m-%d-%Y";
                aggregationInfo.iterationUnit = ChronoUnit.DAYS;
                aggregationInfo.javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_MONTH:
                aggregationInfo.startDateZonedLoop = aggregationInfo.startOfTodayZoned.minusDays(29);
                aggregationInfo.startDateInstant = aggregationInfo.startDateZonedLoop.toInstant();
                aggregationInfo.mongoGroupDateFormat = "%m-%d-%Y";
                aggregationInfo.iterationUnit = ChronoUnit.DAYS;
                aggregationInfo.javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_3_MONTHS:
                aggregationInfo.startDateZonedLoop = aggregationInfo.startOfTodayZoned.minusMonths(2).withDayOfMonth(1);
                aggregationInfo.startDateInstant = aggregationInfo.startDateZonedLoop.toInstant();
                aggregationInfo.mongoGroupDateFormat = "%m-%Y";
                aggregationInfo.iterationUnit = ChronoUnit.MONTHS;
                aggregationInfo.javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_6_MONTHS:
                aggregationInfo.startDateZonedLoop = aggregationInfo.startOfTodayZoned.minusMonths(5).withDayOfMonth(1);
                aggregationInfo.startDateInstant = aggregationInfo.startDateZonedLoop.toInstant();
                aggregationInfo.mongoGroupDateFormat = "%m-%Y";
                aggregationInfo.iterationUnit = ChronoUnit.MONTHS;
                aggregationInfo.javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
                break;
            case PAST_YEAR:
                aggregationInfo.startDateZonedLoop = aggregationInfo.startOfTodayZoned.minusMonths(11).withDayOfMonth(1);
                aggregationInfo.startDateInstant = aggregationInfo.startDateZonedLoop.toInstant();
                aggregationInfo.mongoGroupDateFormat = "%m-%Y";
                aggregationInfo.iterationUnit = ChronoUnit.MONTHS;
                aggregationInfo.javaPeriodFormatter = DateTimeFormatter.ofPattern("MM-yyyy").withZone(ZoneOffset.UTC);
        }
    }

    /**
     * Fetches and aggregates sales data for a line chart based on the specified timeframe.
     *
     * @param timeFrameString The timeframe string (e.g., "PAST_WEEK", "PAST_MONTH").
     * @return A map containing chartLabels, chartSalesData, and chartTitle.
     */
    public Map<String, Object> getSalesDataForLineChart(String timeFrameString) {
        AggregationInfo aggregationInfo = new AggregationInfo();
        setTimeFrameData(aggregationInfo, timeFrameString);

        // --- Aggregation Pipeline ---
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("saleDate").gte(Date.from(aggregationInfo.startDateInstant)).lt(aggregationInfo.endDateExclusive)
        );
        ProjectionOperation projectFieldsForGrouping = Aggregation.project("count", "location")
                .and(DateOperators.DateToString.dateOf("saleDate").toString(aggregationInfo.mongoGroupDateFormat).withTimezone(DateOperators.Timezone.valueOf("UTC"))).as("period");
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
        ZonedDateTime currentLoopDate = aggregationInfo.startDateZonedLoop;

        while (currentLoopDate.toInstant().isBefore(aggregationInfo.endDateExclusive)) {
            String periodKey = currentLoopDate.format(aggregationInfo.javaPeriodFormatter);

            chartLabels.add(periodKey);
            inStoreSalesMap.put(periodKey, 0);
            onlineSalesMap.put(periodKey, 0);
            totalSalesMap.put(periodKey, 0);

            currentLoopDate = currentLoopDate.plus(1, aggregationInfo.iterationUnit);
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
        chartData.put("chartTitle", "Sales Comparison - " + aggregationInfo.timeFrame.toString().replace("_", " ").toLowerCase());

        return chartData;
    }

    /**
     * Creates a dataset for use in a chart, including details such as label, data points,
     * border color and other chart configurations.
     *
     * @param label        the label for the dataset
     * @param data         the data points to be used in the chart, provided as a list of numbers
     * @param borderColor  the color of the border for the chart line in the dataset
     * @return a map containing the dataset configuration with properties such as label, data, fill,
     *         border color, tension and border width
     */
    private Map<String, Object> createDatasetForChart(String label, List<? extends Number> data, String borderColor) {
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", label);
        dataset.put("data", data);
        dataset.put("fill", false);
        dataset.put("borderColor", borderColor);
        dataset.put("tension", 0.1);
        dataset.put("borderWidth", 2);
        return dataset;
    }
    public Map<String, Object> getRevenueDataForLineChart(String timeFrameString) {
        AggregationInfo aggregationInfo = new AggregationInfo();
        setTimeFrameData(aggregationInfo, timeFrameString);

        // --- Aggregation Pipeline for Revenue ---
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("saleDate").gte(Date.from(aggregationInfo.startDateInstant)).lt(aggregationInfo.endDateExclusive)
        );

        // 1. Project to calculate individual sale revenue (count * salePrice)
        //    and format date for grouping, also keep location.
        ProjectionOperation projectIndividualRevenueAndDate = Aggregation.project("location") // Keep location
                .andExpression("multiply(count, salePrice)").as("calculatedRevenue") // revenue = count * salePrice
                .and(DateOperators.DateToString.dateOf("saleDate").toString(aggregationInfo.mongoGroupDateFormat).withTimezone(DateOperators.Timezone.valueOf("UTC"))).as("period");

        // 2. Group by period AND location, sum the calculated 'calculatedRevenue'
        GroupOperation groupOperation = Aggregation.group(Fields.fields("period", "location"))
                .sum("calculatedRevenue").as("totalRevenue"); // Sum the calculated revenue

        // 3. Project to flatten _id and match DTO (period, location, totalRevenue)
        ProjectionOperation projectToDTO = Aggregation.project("totalRevenue")
                .and("_id.period").as("period")
                .and("_id.location").as("location");

        // 4. Sort
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "period", "location");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation, projectIndividualRevenueAndDate, groupOperation, projectToDTO, sortOperation
        );

        AggregationResults<RevenueDataPoint> results = mongoTemplate.aggregate(
                aggregation, Sale.class, RevenueDataPoint.class // Use RevenueDataPoint DTO
        );
        List<RevenueDataPoint> aggregatedDataFromDB = results.getMappedResults();

        // --- Zero-Fill Logic and Data Structuring for Revenue Lines ---
        LinkedHashMap<String, Double> inStoreRevenueMap = new LinkedHashMap<>();
        LinkedHashMap<String, Double> onlineRevenueMap = new LinkedHashMap<>();
        LinkedHashMap<String, Double> totalRevenueMap = new LinkedHashMap<>();

        List<String> chartLabels = new ArrayList<>();
        ZonedDateTime currentLoopDate = aggregationInfo.startDateZonedLoop;

        while (currentLoopDate.toInstant().isBefore(aggregationInfo.endDateExclusive)) {
            String periodKey = currentLoopDate.format(aggregationInfo.javaPeriodFormatter);
            chartLabels.add(periodKey);
            inStoreRevenueMap.put(periodKey, 0.0); // Initialize with 0.0 for Double
            onlineRevenueMap.put(periodKey, 0.0);
            totalRevenueMap.put(periodKey, 0.0);
            currentLoopDate = currentLoopDate.plus(1, aggregationInfo.iterationUnit);
        }

        for (RevenueDataPoint point : aggregatedDataFromDB) {
            String period = point.period;
            String location = point.location;
            Double revenue = point.totalRevenue != null ? point.totalRevenue : 0.0;

            if (location != null && period != null && totalRevenueMap.containsKey(period)) {
                totalRevenueMap.compute(period, (p, currentTotal) -> (currentTotal == null ? 0.0 : currentTotal) + revenue);

                if ("In Store".equalsIgnoreCase(location)) {
                    inStoreRevenueMap.compute(period, (p, currentLocTotal) -> (currentLocTotal == null ? 0.0 : currentLocTotal) + revenue);
                } else if ("Online".equalsIgnoreCase(location)) {
                    onlineRevenueMap.compute(period, (p, currentLocTotal) -> (currentLocTotal == null ? 0.0 : currentLocTotal) + revenue);
                }
            }
        }

        List<Map<String, Object>> datasets = new ArrayList<>();
        datasets.add(createDatasetForChart("In Store Revenue", new ArrayList<>(inStoreRevenueMap.values()), "rgb(255, 159, 64)")); // Orange
        datasets.add(createDatasetForChart("Online Revenue", new ArrayList<>(onlineRevenueMap.values()), "rgb(153, 102, 255)")); // Purple
        datasets.add(createDatasetForChart("Total Revenue", new ArrayList<>(totalRevenueMap.values()), "rgb(255, 205, 86)"));    // Yellow

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("chartLabels", chartLabels);
        chartData.put("datasets", datasets);
        chartData.put("chartTitle", "Revenue Comparison - " + aggregationInfo.timeFrame.toString().replace("_", " ").toLowerCase());
        return chartData;
    }
    /**
     * Imports sales from a CSV file.
     * Assumes CSV format: saleDate, name, type, count, location, cost, salePrice
     *
     * @param file The CSV file to import.
     * @throws IOException If an error occurs during file reading.
     * @throws IllegalArgumentException If the file data is not as expected.
     */
    public void importSalesFromCSV(MultipartFile file) throws IOException {
        List<Sale> salesToSave = new ArrayList<>();
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
                    Sale sale = new Sale();
                    sale.setSaleDate(DateUtils.converStringToInstant(values[0].trim()));
                    sale.setName(values[1].trim());
                    sale.setType(values[2].trim());
                    sale.setCount(Integer.parseInt(values[3].trim()));
                    sale.setLocation(values[4].trim());

                    try {
                        String costString = values[5].trim();
                        if(costString.contains("$")){
                            costString = costString.replace("$", "");
                        }
                        sale.setCost(Float.parseFloat(costString));
                        String salePriceString = values[6].trim();
                        if(salePriceString.contains("$")){
                            salePriceString = salePriceString.replace("$", "");
                        }
                        sale.setSalePrice(Float.parseFloat(salePriceString));
                    } catch (NumberFormatException e) {
                        log.error("Skipping row due to invalid float format: {}. Error: {}", line, e.getMessage());
                        continue;
                    }
                    salesToSave.add(sale);
                } else {
                    log.error("Skipping malformed CSV row: {}", line);
                }
            }
        }

        if (!salesToSave.isEmpty()) {
            saleRepository.saveAll(salesToSave);

        }
    }
}

