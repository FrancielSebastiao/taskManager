package com.fransebastiao.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.calendar.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    // All events in a date range — used for the monthly grid
    @Query("""
        SELECT e FROM Event e
        LEFT JOIN FETCH e.participants
        WHERE e.date >= :from
          AND e.date <= :to
          AND (:privileged = true
               OR EXISTS (
                   SELECT 1 FROM e.participants p WHERE p.id = :userId
               )
               OR e.createdBy.id = :userId
          )
        ORDER BY e.date, e.startTime
    """)
    List<Event> findByDateRange(
        @Param("from")       LocalDate from,
        @Param("to")         LocalDate to,
        @Param("userId")     UUID userId,
        @Param("privileged") boolean privileged
    );

    // Count of events per day — used for upcoming-days badges
    @Query("""
        SELECT e.date, COUNT(e)
        FROM Event e
        WHERE e.date >= :from
          AND e.date <= :to
          AND (:privileged = true
               OR EXISTS (
                   SELECT 1 FROM e.participants p WHERE p.id = :userId
               )
               OR e.createdBy.id = :userId
          )
        GROUP BY e.date
        ORDER BY e.date
    """)
    List<Object[]> countByDay(
        @Param("from")       LocalDate from,
        @Param("to")         LocalDate to,
        @Param("userId")     UUID userId,
        @Param("privileged") boolean privileged
    );
}
