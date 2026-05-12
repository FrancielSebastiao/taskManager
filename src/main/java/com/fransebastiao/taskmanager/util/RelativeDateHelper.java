package com.fransebastiao.taskmanager.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class RelativeDateHelper {
    public String fromDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        Duration diff = Duration.between(dateTime, LocalDateTime.now());
        long minutes = diff.toMinutes();
        long hours = diff.toHours();
        long days = diff.toDays();
        if (minutes < 1) return "Agora mesmo";
        if (minutes < 60) return "Há " + minutes + (minutes == 1 ? " minuto" : " minutos");
        if (hours < 24) return "Há " + hours + (hours == 1 ? " hora" : " horas");
        if (days == 1) return "Ontem";
        if (days < 7) return "Há " + days + " dias";
        if (days < 30) return "Há " + (days / 7) + (days / 7 == 1 ? " semana" : " semanas");
        return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("pt","PT")));
        }
        
    public String fromDate(LocalDate date) {
        if (date == null) return "";
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, date);
        if (days == 0) return "Hoje";
        if (days == 1) return "Amanhã";
        if (days == -1) return "Ontem";
        if (days > 1) return "Em " + days + " dias";
        return "Há " + Math.abs(days) + " dias";
    }
}