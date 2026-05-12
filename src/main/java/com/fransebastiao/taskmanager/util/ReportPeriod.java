package com.fransebastiao.taskmanager.util;

import java.time.LocalDate;
import java.util.Objects;

public enum ReportPeriod {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_QUARTER,
    LAST_YEAR,
    CUSTOM;

    public LocalDate[] toDateRange(LocalDate customFrom, LocalDate customTo) {
        LocalDate now = LocalDate.now();
        return switch (this) {
            case TODAY        -> new LocalDate[]{now, now};
            case LAST_7_DAYS  -> new LocalDate[]{now.minusDays(7), now};
            case LAST_30_DAYS -> new LocalDate[]{now.minusDays(30), now};
            case LAST_QUARTER -> new LocalDate[]{now.minusMonths(3).withDayOfMonth(1), now};
            case LAST_YEAR    -> new LocalDate[]{now.minusYears(1).withDayOfYear(1), now};
            case CUSTOM       -> {
                Objects.requireNonNull(customFrom, "customFrom required for CUSTOM period");
                Objects.requireNonNull(customTo,   "customTo required for CUSTOM period");
                yield new LocalDate[]{customFrom, customTo};
            }
        };
    }
}