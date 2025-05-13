package org.scottishfolds.utility;

import lombok.extern.slf4j.Slf4j;
import org.scottishfolds.requestDTO.CreateSale;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
@Slf4j
public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public static Instant converStringToInstant(String date) {
        Instant instant = null;
        try {
            if (date != null && !date.isEmpty()) {
                LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
                instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            }
        } catch (Exception e) {
            log.error("Error parsing date: {}", e.getMessage());
        }
        return instant;
    }
}
