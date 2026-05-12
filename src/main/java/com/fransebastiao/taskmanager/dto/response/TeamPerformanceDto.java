package com.fransebastiao.taskmanager.dto.response;

public record TeamPerformanceDto(
    String nome,
    Long   totalTasks,
    Long   completedTasks
) {
    public TeamPerformanceDto(String nome, Long totalTasks, Long completedTasks) {
        this.nome           = nome;
        this.totalTasks     = totalTasks;
        this.completedTasks = completedTasks;
    }

    public double completionRate() {
        return totalTasks == 0 ? 0 : Math.round((completedTasks * 100.0 / totalTasks) * 10.0) / 10.0;
    }
}
