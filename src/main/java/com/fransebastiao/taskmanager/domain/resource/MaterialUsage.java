package com.fransebastiao.taskmanager.domain.resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fransebastiao.taskmanager.domain.project.Project;
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
import lombok.Setter;

// MaterialUsage.java — uso de material por projecto
@Entity
@Table(name = "material_usages")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class MaterialUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_MATUSAGE_PROJECT"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_MATUSAGE_MATERIAL"))
    private Material material;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityUsed;

    @Column(nullable = false)
    private LocalDate usageDate;

    @Column(length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by",
        foreignKey = @ForeignKey(name = "FK_MATUSAGE_USER"))
    private User recordedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MaterialUsage(Project project, Material material,
                         BigDecimal quantityUsed, LocalDate usageDate) {
        this.project      = Objects.requireNonNull(project);
        this.material     = Objects.requireNonNull(material);
        this.quantityUsed = Objects.requireNonNull(quantityUsed);
        this.usageDate    = Objects.requireNonNull(usageDate);
    }

    public BigDecimal getTotalCost() {
        return quantityUsed.multiply(material.getUnitPrice());
    }
}
