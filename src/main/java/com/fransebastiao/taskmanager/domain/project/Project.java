package com.fransebastiao.taskmanager.domain.project;

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

import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.domain.task.Task;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"tasks", "team", "category"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANEANDO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private Set<ProjectMember> team = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<MaterialUsage> materials = new ArrayList<>();

    @Column(nullable = true)
    private Integer manualProgress;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
        foreignKey = @ForeignKey(name = "FK_PROJECT_CATEGORY"))
    private ProjectCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.MÉDIA;

    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<ProjectActivity> activities = new ArrayList<>();
    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<ProjectFile> files = new ArrayList<>();

    public enum ProjectStatus { PLANEANDO, EM_PROGRESSO, EM_PAUSA, COMPLETO, CANCELADO }
    public enum Priority   { BAIXA, MÉDIA, ALTA, CRÍTICA }

    public Project(String name, LocalDate startDate, LocalDate deadline, User manager, ProjectCategory category) {
        this.name      = Objects.requireNonNull(name);
        this.startDate = Objects.requireNonNull(startDate);
        this.deadline  = Objects.requireNonNull(deadline);
        this.manager   = Objects.requireNonNull(manager);
        this.category  = Objects.requireNonNull(category); 
    }

    public BigDecimal getTotalLaborCost() {
        return tasks.stream()
                .flatMap(t -> t.getLaborEntries().stream())
                .map(LaborEntry::calculateFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalMaterialCost() {
        return materials.stream()
                .map(MaterialUsage::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCost() {
        return getTotalLaborCost().add(getTotalMaterialCost());
    }

    public int getCompletionRateProgress() {
        long total = tasks.size();
        if (total > 0) {
            long completed = tasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA)
                .count();
            return (int) Math.round((completed * 100.0) / total);
        }
        return manualProgress != null ? manualProgress : 0;
    }

    public int calculateProgress() {
        if (tasks == null || tasks.isEmpty()) {
            return manualProgress != null ? manualProgress : 0; 
        }

        return (int) Math.round(
            tasks.stream()
                .mapToInt(Task::getProgressPercent)
                .average()
                .orElse(0)
        );
    }
}
