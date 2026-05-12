package com.fransebastiao.taskmanager.domain.task;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
@Table(name = "task_comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"task", "author"})
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, length = 36, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_COMMENT_TASK"))
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_COMMENT_AUTHOR"))
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommentCategory category;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_file_id",
    foreignKey = @ForeignKey(name = "FK_COMMENT_FILE"))
    private TaskFile attachmentFile;

    public enum CommentCategory {
        MATERIAL_SHORTAGE,
        WEATHER_CONDITIONS,
        EQUIPMENT_FAILURE,
        WAITING_FOR_APPROVAL,
        OTHER
    }

    public void setAttachmentFile(TaskFile file) {
        this.attachmentFile = file;
    }

    public TaskComment(Task task, User author,
                       CommentCategory category, String content) {
        this.task     = Objects.requireNonNull(task);
        this.author   = Objects.requireNonNull(author);
        this.category = Objects.requireNonNull(category);
        this.content  = content;
    }

    public void editar(CommentCategory category, String content) {
        this.category = Objects.requireNonNull(category);
        this.content  = content;
    }
}
