package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectSummaryResponse(
    UUID              id,
    String            name,
    String            description,
    String            status,
    String            priority,
    String            category,
    Integer           progress,       // % calculado das tarefas
    LocalDate         deadline,
    BigDecimal        budget,
    BigDecimal        spent,          // custo real acumulado
    TaskBreakdownDto  tasks,
    List<MemberAvatarDto> team
) {}
