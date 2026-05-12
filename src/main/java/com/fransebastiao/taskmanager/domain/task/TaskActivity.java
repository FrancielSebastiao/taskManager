package com.fransebastiao.taskmanager.domain.task;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fransebastiao.taskmanager.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "task_activities")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"task", "user"})
public class TaskActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false,
    foreignKey = @ForeignKey(name = "FK_TASKACTIVITY_TASK"))
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
    foreignKey = @ForeignKey(name = "FK_TASKACTIVITY_USER"))
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActivityType {
        TASK_CREATED,
        PROGRESS_UPDATED,
        STATUS_CHANGED,
        COMMENT_ADDED,
        FILE_UPLOADED,
        PHOTO_UPLOADED,
        ASSIGNEE_ADDED,
        ASSIGNEE_REMOVED,
        DEADLINE_CHANGED,
        TASK_COMPLETED
    }

    public TaskActivity(Task task, User user, String text, ActivityType type) {
        this.task = Objects.requireNonNull(task);
        this.user = Objects.requireNonNull(user);
        this.text = Objects.requireNonNull(text);
        this.type = Objects.requireNonNull(type);
    }

    public String resolveMarkerClass() {
        return switch (type) {
            case PROGRESS_UPDATED -> "marker--blue";
            case PHOTO_UPLOADED, FILE_UPLOADED -> "marker--purple";
            case COMMENT_ADDED -> "marker--green";
            case DEADLINE_CHANGED -> "marker--amber";
            case TASK_CREATED -> "marker--blue";
            default -> "marker--gray";
        };
    }
}
