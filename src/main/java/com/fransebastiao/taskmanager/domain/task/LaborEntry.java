package com.fransebastiao.taskmanager.domain.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "labor_entries")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"task", "worker"})
public class LaborEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_LABOR_TASK"))
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_LABOR_WORKER"))
    private User worker;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expectedEndDate;

    private LocalDate actualEndDate;

    /**
     * Valor prometido ao trabalhador por concluir o trabalho no prazo.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public LaborEntry(Task task, User worker, LocalDate startDate,
                      LocalDate expectedEndDate, BigDecimal agreedAmount) {
        this.task            = Objects.requireNonNull(task);
        this.worker          = Objects.requireNonNull(worker);
        this.startDate       = Objects.requireNonNull(startDate);
        this.expectedEndDate = Objects.requireNonNull(expectedEndDate);
        this.agreedAmount    = Objects.requireNonNull(agreedAmount);
        validateDates();
    }

    // -------------------------------------------------------------------------
    // Métodos de negócio
    // -------------------------------------------------------------------------

    /**
     * Regista a conclusão do trabalho.
     */
    public void complete(LocalDate actualEndDate) {
        Objects.requireNonNull(actualEndDate, "actualEndDate não pode ser nulo");
        if (actualEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Data de conclusão não pode ser anterior à data de início");
        }
        this.actualEndDate = actualEndDate;
    }

    /**
     * Número de dias previstos para concluir o trabalho.
     */
    public long getAllocatedDays() {
        return ChronoUnit.DAYS.between(startDate, expectedEndDate);
    }

    /**
     * Número de dias que o trabalho demorou a ser concluído.
     * Requer que o trabalho esteja concluído.
     */
    public long getActualDays() {
        ensureCompleted();
        return ChronoUnit.DAYS.between(startDate, actualEndDate);
    }

    /**
     * Dias de diferença entre o previsto e o real.
     * Positivo = atrasado. Negativo = antecipado.
     */
    public long getDaysDifference() {
        return getActualDays() - getAllocatedDays();
    }

    /**
     * Calcula o valor final a receber pelo trabalhador:
     *
     * - Concluído a tempo:       agreedAmount
     * - Concluído com atraso:    agreedAmount - (daysLate / allocatedDays) * agreedAmount
     *                            mínimo garantido: 0
     * - Concluído antecipado:    agreedAmount + (daysSaved / allocatedDays) * agreedAmount
     */
    public BigDecimal calculateFinalAmount() {
        ensureCompleted();

        long allocatedDays = getAllocatedDays();

        if (allocatedDays == 0) {
            return agreedAmount;
        }

        long diff = getDaysDifference();

        if (diff == 0) {
            // Concluído exactamente a tempo
            return agreedAmount;
        }

        BigDecimal ratio = BigDecimal.valueOf(Math.abs(diff))
                .divide(BigDecimal.valueOf(allocatedDays), 10, RoundingMode.HALF_UP);

        BigDecimal adjustment = agreedAmount.multiply(ratio);

        if (diff > 0) {
            // Atrasado — aplica desconto, mínimo de zero
            return agreedAmount.subtract(adjustment)
                    .max(BigDecimal.ZERO);
        } else {
            // Antecipado — aplica bónus
            return agreedAmount.add(adjustment);
        }
    }

    /**
     * Valor do desconto aplicado (zero se não houve atraso).
     */
    public BigDecimal getDiscount() {
        ensureCompleted();
        if (getDaysDifference() <= 0) return BigDecimal.ZERO;
        return agreedAmount.subtract(calculateFinalAmount());
    }

    /**
     * Valor do bónus aplicado (zero se não houve antecipação).
     */
    public BigDecimal getBonus() {
        ensureCompleted();
        if (getDaysDifference() >= 0) return BigDecimal.ZERO;
        return calculateFinalAmount().subtract(agreedAmount);
    }

    public boolean isCompleted() {
        return actualEndDate != null;
    }

    public boolean isLate() {
        ensureCompleted();
        return getDaysDifference() > 0;
    }

    public boolean isEarly() {
        ensureCompleted();
        return getDaysDifference() < 0;
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void ensureCompleted() {
        if (!isCompleted()) {
            throw new IllegalStateException("O trabalho ainda não foi concluído");
        }
    }

    private void validateDates() {
        if (!expectedEndDate.isAfter(startDate)) {
            throw new IllegalArgumentException("expectedEndDate deve ser posterior a startDate");
        }
    }
}
