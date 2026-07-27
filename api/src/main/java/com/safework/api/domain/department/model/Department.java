package com.safework.api.domain.department.model;

import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"organization", "manager", "employees"})
@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "name"})
})
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name; // e.g., "IT", "Finance", "Operations"

    @Column(length = 500)
    private String description;

    @Column(length = 10)
    private String code; // Short department code, e.g., "IT", "FIN", "OPS"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id")
    private User manager; // Department manager

    @Column
    private Integer employeeCount; // Cached count for reporting

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    // --- Relationships ---

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> employees;
}
