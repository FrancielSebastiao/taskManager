package com.fransebastiao.taskmanager.util;

import java.time.LocalDate;

public enum DashboardPeriod {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_90_DAYS;

    public LocalDate[] toDateRange() {
        LocalDate now = LocalDate.now();

        LocalDate from = switch (this) {
            case TODAY        -> now;
            case LAST_7_DAYS  -> now.minusDays(6);
            case LAST_30_DAYS -> now.minusDays(29);
            case LAST_90_DAYS -> now.minusDays(89);
        };

        return new LocalDate[]{from, now};
    }
}