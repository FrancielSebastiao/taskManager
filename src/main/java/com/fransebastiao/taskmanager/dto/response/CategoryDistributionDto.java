package com.fransebastiao.taskmanager.dto.response;

public record CategoryDistributionDto(
    String categoryName,
    Long   count
) {
    public CategoryDistributionDto(String categoryName, Long count) {
        this.categoryName = categoryName;
        this.count        = count;
    }
}
