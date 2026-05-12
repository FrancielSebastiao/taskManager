package com.fransebastiao.taskmanager.domain.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fransebastiao.taskmanager.domain.attachment.TaskPhoto;
import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.user.User;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "tasks",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_TASK_PROJECT_TITLE", columnNames = { "project_id", "title" })
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"project", "laborEntries", "assignees", "photos", "comments", "files", "activities"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.MÉDIA;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "FK_TASK_CREATED_BY"))
    private User createdBy;

    @Column(precision = 5, scale = 1)
    private BigDecimal estimatedHours;

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<TaskFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<TaskActivity> activities = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_TASK_PROJECT"))
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_assignees",
        joinColumns        = @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_TASK_ASSIGNEES_TASK")),
        inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_TASK_ASSIGNEES_USER"))
    )
    private Set<User> assignees = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<LaborEntry> laborEntries = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<TaskPhoto> photos = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer progressPercent = 0;

    @Column(length = 36)
    private String lastProgressUpdatedById;

    private LocalDateTime lastProgressUpdatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "FK_TASK_CATEGORY"))
    private TaskCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<Task> subtasks = new ArrayList<>();

    public enum TaskStatus { PENDENTE, EM_PROGRESSO, ESPERANDO_APROVAÇÃO, COMPLETA, BLOQUEADA }
    public enum Priority   { BAIXA, MÉDIA, ALTA, CRÍTICA }

    public Task(String title, LocalDate dueDate, Project project, TaskCategory category) {
        this.title   = Objects.requireNonNull(title);
        this.dueDate = Objects.requireNonNull(dueDate);
        this.project = project;
        this.category = Objects.requireNonNull(category);
    }

    public void actualizarProgresso(Integer percent, User updatedBy) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        // Se concluída, progresso é sempre 100%
        if (this.status == TaskStatus.COMPLETA) {
            throw new IllegalStateException("Cannot update progress on a completed task");
        }
        this.progressPercent          = percent;
        this.lastProgressUpdatedById  = updatedBy.getId().toString();
        this.lastProgressUpdatedAt    = LocalDateTime.now();

        // Actualiza status automaticamente consoante o progresso
        if (percent == 0) {
            this.status = TaskStatus.PENDENTE;
        } else if (percent < 100) {
            this.status = TaskStatus.EM_PROGRESSO;
        }
    }

    public void complete() {
        this.status           = TaskStatus.COMPLETA;
        this.completedAt      = LocalDateTime.now();
        this.progressPercent  = 100;
    }

    public boolean isOverdue() {
        return status != TaskStatus.COMPLETA && LocalDate.now().isAfter(dueDate);
    }

    public void adicionarAssignee(User user) {
        Objects.requireNonNull(user, "user não pode ser nulo");
        this.assignees.add(user);
    }

    public void removerAssignee(User user) {
        this.assignees.remove(user);
    }

    public boolean temAssignee(User user) {
        return this.assignees.contains(user);
    }

    public void addSubtask(Task subtask) {
        subtask.parentTask = this;
        this.subtasks.add(subtask);
    }

    public boolean isSubtask() {
        return parentTask != null;
    }
}
