package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;

public record UpcomingDayDto(
    LocalDate   date,
    String      label,      // "Amanhã", "Terça-Feira" …
    long        count
) {

}
