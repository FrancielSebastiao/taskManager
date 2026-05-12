package com.fransebastiao.taskmanager.domain.attachment;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "task_photos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = "task")
public class TaskPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_PHOTO_TASK"))
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false,
        foreignKey = @ForeignKey(name = "FK_PHOTO_USER"))
    private User uploadedBy;

    // Chave única no S3 — ex: tasks/{taskId}/photos/{uuid}.jpg
    @Column(nullable = false)
    private String s3Key;

    // URL pré-assinada gerada dinamicamente — não persistida
    @Transient
    private String url;

    @Column(nullable = false, length = 10)
    private String extension; // jpg ou png

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(length = 255)
    private String caption;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public TaskPhoto(Task task, User uploadedBy, String s3Key,
                     String extension, Long fileSizeBytes) {
        this.task          = Objects.requireNonNull(task);
        this.uploadedBy    = Objects.requireNonNull(uploadedBy);
        this.s3Key         = Objects.requireNonNull(s3Key);
        this.extension     = Objects.requireNonNull(extension);
        this.fileSizeBytes = Objects.requireNonNull(fileSizeBytes);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}