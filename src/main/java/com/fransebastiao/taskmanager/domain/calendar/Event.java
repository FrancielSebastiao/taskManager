package com.fransebastiao.taskmanager.domain.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fransebastiao.taskmanager.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventColor color = EventColor.BLUE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_participants",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum EventColor { PINK, BLUE, PURPLE }

    // Convenience
    public int getParticipantCount() { return participants.size(); }

    public String getFormattedTime() {
        if (startTime == null) return null;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        if (endTime == null) return startTime.format(fmt);
        return startTime.format(fmt) + " - " + endTime.format(fmt);
    }

    public void addParticipant(User user) {
        Objects.requireNonNull(user, "user não pode ser nulo");
        this.participants.add(user);
    }
}