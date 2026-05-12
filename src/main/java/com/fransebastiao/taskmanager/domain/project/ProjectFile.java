package com.fransebastiao.taskmanager.domain.project;

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
@Table(name = "project_files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"project", "uploadedBy"})
public class ProjectFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_FILE_PROJECT"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by",
        foreignKey = @ForeignKey(name = "FK_FILE_USER"))
    private User uploadedBy;

    @Column(nullable = false)
    private String originalName;  // ex: design-system.fig

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false, length = 20)
    private String extension;     // fig, pdf, zip, etc.

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public ProjectFile(Project project, User uploadedBy, String originalName,
                       String s3Key, Long fileSizeBytes, String extension) {
        this.project      = Objects.requireNonNull(project);
        this.uploadedBy   = Objects.requireNonNull(uploadedBy);
        this.originalName = Objects.requireNonNull(originalName);
        this.s3Key        = Objects.requireNonNull(s3Key);
        this.fileSizeBytes = Objects.requireNonNull(fileSizeBytes);
        this.extension    = Objects.requireNonNull(extension);
    }

    // Ícone e cor baseados na extensão — lógica de apresentação no DTO
    public String resolveIcon() {
        return switch (extension.toLowerCase()) {
            case "pdf"              -> "picture_as_pdf";
            case "fig", "sketch"   -> "insert_drive_file";
            case "zip", "rar"      -> "folder_zip";
            case "png", "jpg",
                 "jpeg", "webp"    -> "image";
            case "xlsx", "csv"     -> "table_chart";
            case "docx", "doc"     -> "description";
            default                -> "attach_file";
        };
    }

    public String resolveIconBgClass() {
        return switch (extension.toLowerCase()) {
            case "pdf"             -> "icon-bg--red";
            case "fig", "sketch"  -> "icon-bg--purple";
            case "zip", "rar"     -> "icon-bg--amber";
            case "png", "jpg",
                 "jpeg", "webp"   -> "icon-bg--green";
            case "xlsx", "csv"    -> "icon-bg--teal";
            default               -> "icon-bg--blue";
        };
    }

    public String resolveIconColorClass() {
        return resolveIconBgClass().replace("bg--", "--");
    }

    public String formatSize() {
        if (fileSizeBytes < 1024)           return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024)    return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024));
    }
}
