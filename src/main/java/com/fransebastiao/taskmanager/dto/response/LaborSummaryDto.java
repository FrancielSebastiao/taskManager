package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LaborSummaryDto(
    String     workerName,
    String     taskTitle,
    LocalDate  startDate,
    LocalDate  expectedEndDate,
    LocalDate  actualEndDate,
    BigDecimal agreedAmount
) {
    public LaborSummaryDto(String workerName, String taskTitle, LocalDate startDate,
                            LocalDate expectedEndDate, LocalDate actualEndDate,
                            BigDecimal agreedAmount) {
        this.workerName      = workerName;
        this.taskTitle       = taskTitle;
        this.startDate       = startDate;
        this.expectedEndDate = expectedEndDate;
        this.actualEndDate   = actualEndDate;
        this.agreedAmount    = agreedAmount;
    }

    public long getAllocatedDays() {
        return ChronoUnit.DAYS.between(startDate, expectedEndDate);
    }

    public Long getActualDays() {
        return actualEndDate != null
                ? ChronoUnit.DAYS.between(startDate, actualEndDate)
                : null;
    }

    public BigDecimal getFinalAmount() {
        if (actualEndDate == null) return null;
        long diff      = getActualDays() - getAllocatedDays();
        long allocated = getAllocatedDays();
        if (allocated == 0 || diff == 0) return agreedAmount;
        BigDecimal ratio = BigDecimal.valueOf(Math.abs(diff))
                .divide(BigDecimal.valueOf(allocated), 10, RoundingMode.HALF_UP);
        BigDecimal adj = agreedAmount.multiply(ratio);
        return diff > 0
                ? agreedAmount.subtract(adj).max(BigDecimal.ZERO)
                : agreedAmount.add(adj);
    }

    public String getLaborType() {
        if (actualEndDate == null) return "PENDING";
        long diff = getActualDays() - getAllocatedDays();
        return diff == 0 ? "ON_TIME" : diff < 0 ? "BONUS" : "DISCOUNT";
    }
}