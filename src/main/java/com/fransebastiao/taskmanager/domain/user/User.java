package com.fransebastiao.taskmanager.domain.user;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fransebastiao.taskmanager.domain.task.Task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"passwordHash", "roles"})
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "users_roles",
        joinColumns        = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<Task> assignedTasks = new HashSet<>();

    public User(String name, String email, String passwordHash) {
        this.name = Objects.requireNonNull(name);
        this.email = email.toLowerCase();
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    // -------------------------------------------------------------------------
    // Outros métodos de negócio
    // -------------------------------------------------------------------------

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
    public void updatePassword(String hash) { this.passwordHash = hash; }
}